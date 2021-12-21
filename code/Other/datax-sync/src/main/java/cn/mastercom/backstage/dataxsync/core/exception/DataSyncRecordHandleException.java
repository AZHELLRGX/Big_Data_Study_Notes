package cn.mastercom.backstage.dataxsync.core.exception;

/**
 * @author 饶舸璇
 * @description 同步任务处理过程异常
 * @Date 2020/12/14 17:09
 */
public class DataSyncRecordHandleException extends Exception {
	public DataSyncRecordHandleException() {
		super();
	}

	public DataSyncRecordHandleException(String message) {
		super(message);
	}
}
