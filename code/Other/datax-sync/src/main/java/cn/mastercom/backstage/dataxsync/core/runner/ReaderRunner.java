package cn.mastercom.backstage.dataxsync.core.runner;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.RunnerException;
import cn.mastercom.backstage.dataxsync.core.plugin.Reader;
import cn.mastercom.backstage.dataxsync.core.transport.RecordSender;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.dataxservice.face.domain.enums.State;

import java.util.concurrent.CountDownLatch;

public class ReaderRunner implements Runnable {

	private boolean overFLag = false;

	private RecordSender recordSender;

	private Reader taskReader;
	private DataSyncRecord dataSyncRecord;
	private TaskConfig taskConfig;
	private Communication communication;
	private CountDownLatch countDownLatch;

	public ReaderRunner(Communication communication, RecordSender recordSender, Reader taskReader, TaskConfig taskConfig,
			DataSyncRecord dataSyncRecord,
			CountDownLatch countDownLatch) {
		this.communication = communication;
		this.recordSender = recordSender;
		this.taskReader = taskReader;
		this.taskConfig = taskConfig;
		this.dataSyncRecord = dataSyncRecord;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {
		try {
			taskReader.init(taskConfig, dataSyncRecord);
			taskReader.prepare();
			taskReader.startRead(recordSender);
			recordSender.terminate();
		} catch (DataXException e) {
			// do nothing
		} catch (Exception e) {
			// 其他非DataXException需要处理
			taskReader.markFail(e);
			communication.setState(State.FAILED);
			throw new RunnerException("Reader runner Received Exceptions");
		} finally {
			taskReader.destroy();
			// 结束
			countDownLatch.countDown();
			overFLag = true;
		}
	}

	public void shutdown() {
		recordSender.shutdown();
		// 线程被终止的时候，run方法的finally块代码可能不会执行，需要手动判断，然后考虑是否重新执行一下
		if (!overFLag) {
			taskReader.destroy();
			// 结束
			countDownLatch.countDown();
		}
	}
}
