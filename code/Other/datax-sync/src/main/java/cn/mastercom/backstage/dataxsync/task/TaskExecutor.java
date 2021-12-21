package cn.mastercom.backstage.dataxsync.task;

import cn.mastercom.backstage.dataxsync.config.ChannelConfig;
import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.ChannelException;
import cn.mastercom.backstage.dataxsync.core.plugin.Reader;
import cn.mastercom.backstage.dataxsync.core.plugin.Writer;
import cn.mastercom.backstage.dataxsync.core.runner.ReaderRunner;
import cn.mastercom.backstage.dataxsync.core.runner.WriterRunner;
import cn.mastercom.backstage.dataxsync.core.transport.*;
import cn.mastercom.backstage.dataxsync.dao.MainMapper;
import cn.mastercom.backstage.dataxsync.plugin.mysql.reader.MysqlReader;
import cn.mastercom.backstage.dataxsync.plugin.mysql.writer.MysqlWriter;
import cn.mastercom.backstage.dataxsync.plugin.oracle.reader.OracleReader;
import cn.mastercom.backstage.dataxsync.plugin.oracle.writer.OracleWriter;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.reader.SqlServerReader;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.reader.SqlServerReaderBcp;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.writer.SqlServerWriter;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.writer.SqlServerWriterBcp;
import cn.mastercom.backstage.dataxsync.plugin.sybaseiq.reader.SybaseIqReader;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import cn.mastercom.backstage.dataxsync.pojo.Task;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.core.statistics.communication.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.alibaba.datax.plugin.rdbms.util.DataBaseType.*;

public class TaskExecutor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

	private MainMapper mainMapper;
	private TaskConfig taskConfig;
	private ChannelConfig channelConfig;
	private Task task;
	private Queue<DataSyncRecord> records;

	public TaskExecutor(MainMapper mainMapper, TaskConfig taskConfig, ChannelConfig channelConfig, Task task) {
		this.mainMapper = mainMapper;
		this.taskConfig = taskConfig;
		this.channelConfig = channelConfig;
		this.task = task;
		this.records = task.getRecordQueue();
	}

	@Override
	public void run() {
		while(!records.isEmpty()){
			DataSyncRecord record = records.poll();
			handleOneSyncTask(record);
		}

		synchronized (StaticData.getTaskMap()){
			if(records.isEmpty()){
				StaticData.getTaskMap().remove(task.getTable());
				return;
			}
		}

		run();
	}

    private void handleOneSyncTask(DataSyncRecord dataSyncRecord) {
        try {
            // 检测任务是否可执行
            if (dataSyncRecord.isBcp()) {
                // 如果配置的任务执行方式是bcp方式
                handleDataSyncRecordByBcp(dataSyncRecord);
            } else {
                handleDataSyncRecord(dataSyncRecord);
            }
        } catch (Exception e) {
            LOG.error("id为{}的数据同步记录处理出错", dataSyncRecord.getGuid(), e);
        } finally {
            // 更新记录状态，记录日志
            mainMapper.updateDataSyncRecord(dataSyncRecord.getStatusId(), dataSyncRecord.getGuid());
            mainMapper.insertDataSyncLog(dataSyncRecord.getDataSyncLog(), dataSyncRecord.getDateFormat());
        }
    }

    /**
	 * 使用旧版本的bcp导入导出的方式
	 *
	 * @param dataSyncRecord
	 */
	private void handleDataSyncRecordByBcp(DataSyncRecord dataSyncRecord) {
		// 导出和导入必须同步进行
		// 只开启一个线程，先使用bcp out；再使用bcp in
		dataSyncRecord.getDataSyncLog().setBeginTime(new Date());

		SqlServerReaderBcp reader = new SqlServerReaderBcp();
		try {
			LOG.info("{}的bcp导出开始........", dataSyncRecord.getGuid());
			reader.init(taskConfig, dataSyncRecord);
			reader.prepare();
			reader.startRead();
			LOG.info("{}的bcp导出完成........", dataSyncRecord.getGuid());
		} catch (Exception e) {
			reader.markFail(e);
			reader.destroy(); // 导出阶段异常也需要及时将文件删除
			LOG.info("{}的bcp导出错误........", dataSyncRecord.getGuid());
			// 一定要加上这个，不然日志插入会报异常
			dataSyncRecord.getDataSyncLog().setEndTime(new Date());
			return;
		}

		SqlServerWriterBcp writer = new SqlServerWriterBcp();
		try {
			LOG.info("{}的bcp导入开始........", dataSyncRecord.getGuid());
			writer.init(taskConfig, dataSyncRecord);
			writer.prepare();
			writer.startWrite();
			writer.post();
			writer.markSuccess();
			LOG.info("{}的bcp导入完成........", dataSyncRecord.getGuid());
		} catch (Exception e) {
			writer.markFail(e);
			LOG.info("{}的bcp导入错误........", dataSyncRecord.getGuid());
		} finally {
			writer.destroy(); // 无论是否成功，删除bcp文件，防止堆积
		}
		dataSyncRecord.getDataSyncLog().setEndTime(new Date());
	}

	private void handleDataSyncRecord(DataSyncRecord dataSyncRecord) throws ChannelException, InterruptedException {
		CountDownLatch countDownLatchSub = new CountDownLatch(2);
		LOG.info("开始处理任务:{}", dataSyncRecord.getGuid());
		// 核心同步模块，采用dataX的方式
		// 初始化writerRunner和readerRunner
		StaticData.registerTaskCommunication(dataSyncRecord.getGuid());
		Channel channel = new MemoryChannel(channelConfig);
		Communication communication = StaticData.getTaskCommunication(dataSyncRecord.getGuid());
		channel.setCommunication(communication);

		dataSyncRecord.getDataSyncLog().setBeginTime(new Date());
		// writerRunner
		ServerConfigInfo serverConfigInfoTarget = StaticData.getServerConfigInfo(dataSyncRecord.getTargetServerId());
		Writer writer = null;
		if (serverConfigInfoTarget.getType().equalsIgnoreCase(MySql.getTypeName())) {
			writer = new MysqlWriter();
		} else if (serverConfigInfoTarget.getType().equalsIgnoreCase(SQLServer.getTypeName())) {
			writer = new SqlServerWriter();
		} else if (serverConfigInfoTarget.getType().equalsIgnoreCase(Oracle.getTypeName())) {
			writer = new OracleWriter();
		}

		RecordReceiver<Record> recordReceiver = new BufferedRecordExchanger(channel);
		WriterRunner writerRunner = new WriterRunner(communication, recordReceiver, writer, taskConfig, dataSyncRecord, countDownLatchSub);
		Thread writerThread = new Thread(writerRunner);
		writerThread.start();

		// readerRunner
		ServerConfigInfo serverConfigInfoSource = StaticData.getServerConfigInfo(dataSyncRecord.getSourceServerId());
		Reader reader = null;
		if (serverConfigInfoSource.getType().equalsIgnoreCase(MySql.getTypeName())) {
			reader = new MysqlReader();
		} else if (serverConfigInfoSource.getType().equalsIgnoreCase(SQLServer.getTypeName())) {
			reader = new SqlServerReader();
		} else if (serverConfigInfoSource.getType().equalsIgnoreCase(Oracle.getTypeName())) {
			reader = new OracleReader();
		} else if (serverConfigInfoSource.getType().equalsIgnoreCase(SybaseIQ.getTypeName())) {
			reader = new SybaseIqReader();
		}
		RecordSender<Record> recordSender = new BufferedRecordExchanger(channel);
		ReaderRunner readerRunner = new ReaderRunner(communication, recordSender, reader, taskConfig, dataSyncRecord, countDownLatchSub);
		Thread readerThread = new Thread(readerRunner);
		readerThread.start();

		// 读写线程互相监听，一个出现问题，全部停止
		writerThread.setUncaughtExceptionHandler((t, e) -> shutdown(readerRunner, writerRunner, readerThread, writerThread));

		readerThread.setUncaughtExceptionHandler((t, e) -> shutdown(readerRunner, writerRunner, readerThread, writerThread));

		// 设置最长等待时间
		boolean await = countDownLatchSub.await(2, TimeUnit.HOURS);
		dataSyncRecord.getDataSyncLog().setEndTime(new Date());
		if (!await) {
			// 如果是超时结束的，需要强制杀死所有读写线程
			shutdown(readerRunner, writerRunner, readerThread, writerThread);
		}
		if (dataSyncRecord.getStatusId() == 2) {
			if (await) {
				dataSyncRecord.setStatusId(4);
			} else {
				dataSyncRecord.setStatusId(3); // 任务执行超时
			}
		}
		LOG.info("任务{}处理完成", dataSyncRecord.getGuid());
	}

	private void shutdown(ReaderRunner readerRunner, WriterRunner writerRunner, Thread readerThread, Thread writerThread) {
		writerRunner.shutdown();
		readerRunner.shutdown();
		if (writerThread.isAlive()) {
			writerThread.interrupt();
		}
		if (readerThread.isAlive()) {
			readerThread.interrupt();
		}
	}

}
