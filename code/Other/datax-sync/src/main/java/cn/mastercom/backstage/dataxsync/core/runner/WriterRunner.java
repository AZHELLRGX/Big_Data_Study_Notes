package cn.mastercom.backstage.dataxsync.core.runner;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.RunnerException;
import cn.mastercom.backstage.dataxsync.core.plugin.Writer;
import cn.mastercom.backstage.dataxsync.core.transport.RecordReceiver;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.dataxservice.face.domain.enums.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class WriterRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(WriterRunner.class);

	private boolean overFlag;

	private RecordReceiver recordReceiver;
	private Writer taskWriter;

	private TaskConfig taskConfig;
	private DataSyncRecord dataSyncRecord;
	private Communication communication;
	private CountDownLatch countDownLatch;

	public WriterRunner(Communication communication, RecordReceiver recordReceiver, Writer writer, TaskConfig taskConfig,
			DataSyncRecord dataSyncRecord,
			CountDownLatch countDownLatch) {
		this.communication = communication;
		this.recordReceiver = recordReceiver;
		this.taskWriter = writer;
		this.dataSyncRecord = dataSyncRecord;
		this.taskConfig = taskConfig;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {
		try {
			taskWriter.init(taskConfig, dataSyncRecord);
			taskWriter.prepare();
			taskWriter.startWrite(recordReceiver);
			taskWriter.post();
			taskWriter.markSuccess();
		} catch (DataXException e) {
			// do nothing
		} catch (Exception e) {
			// 其他的非DataXException异常需要处理
			taskWriter.markFail(e);
			communication.setState(State.FAILED);
			throw new RunnerException("Writer Runner Received Exceptions");
		} finally {
			LOG.debug("task writer starts to do destroy ...");
			taskWriter.destroy();
			countDownLatch.countDown();
			overFlag = true;
		}
	}

	public void shutdown() {
		recordReceiver.shutdown();
		if (!overFlag) {
			taskWriter.destroy();
			countDownLatch.countDown();
		}
	}
}
