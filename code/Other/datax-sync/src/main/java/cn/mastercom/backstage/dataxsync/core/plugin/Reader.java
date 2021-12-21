package cn.mastercom.backstage.dataxsync.core.plugin;

import cn.mastercom.backstage.dataxsync.core.transport.RecordSender;
import com.alibaba.druid.pool.DruidPooledConnection;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.rdbms.CommonRdbmsReader;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import cn.mastercom.backstage.dataxsync.util.StaticData;

public abstract class Reader {
	protected DruidPooledConnection druidPooledConnection;

	protected DataSyncRecord dataSyncRecord;

	protected CommonRdbmsReader commonRdbmsReader;

	public void init(TaskConfig taskConfig, DataSyncRecord dataSyncRecord) {
		druidPooledConnection = StaticData.getPooledConnectionByServerId(dataSyncRecord.getSourceServerId());
		this.dataSyncRecord = dataSyncRecord;
		this.commonRdbmsReader = new CommonRdbmsReader(taskConfig);
	}

	public void startRead(RecordSender recordSender) {
		this.commonRdbmsReader.startRead(dataSyncRecord, recordSender, druidPooledConnection.getConnection());
	}

	public abstract void prepare();

	public void destroy() {
		// 归还连接
		CommonUtil.recycle(druidPooledConnection);
	}

	public void markFail(Throwable e) {
		// 记录日志到数据库，读取失败
		dataSyncRecord.setStatusId(5);
		dataSyncRecord.getDataSyncLog().setInfo(e.getMessage());
		dataSyncRecord.getDataSyncLog().setSuccess(false);
	}
}
