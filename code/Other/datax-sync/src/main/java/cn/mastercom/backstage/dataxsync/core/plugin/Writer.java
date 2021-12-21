package cn.mastercom.backstage.dataxsync.core.plugin;

import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.druid.pool.DruidPooledConnection;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.WriterException;
import cn.mastercom.backstage.dataxsync.core.rdbms.CommonRdbmsWriter;
import cn.mastercom.backstage.dataxsync.core.transport.RecordReceiver;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import cn.mastercom.backstage.dataxsync.util.StaticData;

public abstract class Writer {
	protected DataSyncRecord dataSyncRecord;

	protected TaskConfig taskConfig;
	protected DruidPooledConnection druidPooledConnection;
	protected CommonRdbmsWriter commonRdbmsWriter;
	protected DataBaseType dataBaseType;

	public void init(TaskConfig taskConfig, DataSyncRecord dataSyncRecord) {
		this.taskConfig = taskConfig;
		this.dataSyncRecord = dataSyncRecord;
		this.druidPooledConnection = StaticData.getPooledConnectionByServerId(dataSyncRecord.getTargetServerId());
		this.commonRdbmsWriter = new CommonRdbmsWriter(dataBaseType);
	}

	public void prepare() throws WriterException {
		this.commonRdbmsWriter.prepare(dataSyncRecord, taskConfig, druidPooledConnection.getConnection());
	}

	public void startWrite(RecordReceiver recordReceiver) throws WriterException {
		this.commonRdbmsWriter.startWrite(recordReceiver, druidPooledConnection.getConnection());
	}

	public abstract void post() throws WriterException;

	public void destroy() {
		CommonUtil.recycle(druidPooledConnection);
	}

	public void markSuccess() {
		dataSyncRecord.setStatusId(4);
		dataSyncRecord.getDataSyncLog().setSuccess(true);
		dataSyncRecord.getDataSyncLog().setInfo("数据同步成功");
	}

	public void markFail(Throwable e) {
		dataSyncRecord.setStatusId(6);
		dataSyncRecord.getDataSyncLog().setInfo(e.getMessage());
		dataSyncRecord.getDataSyncLog().setSuccess(false);
	}
}
