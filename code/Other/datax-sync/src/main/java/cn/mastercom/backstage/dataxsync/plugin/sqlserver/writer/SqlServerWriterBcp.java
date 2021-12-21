package cn.mastercom.backstage.dataxsync.plugin.sqlserver.writer;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.WriterException;
import cn.mastercom.backstage.dataxsync.core.rdbms.CommonRdbmsWriter;
import cn.mastercom.backstage.dataxsync.dao.CommonCrud;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.common.SqlServerCommonSql;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import cn.mastercom.backstage.dataxsync.util.CmdUtils;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

public class SqlServerWriterBcp {

	private static final DataBaseType DATABASE_TYPE = DataBaseType.SQLServer;

	private DataSyncRecord dataSyncRecord;

	private TaskConfig taskConfig;

	private CommonRdbmsWriter commonRdbmsWriter;

	private String path;

	private DruidPooledConnection druidPooledConnection;

	private boolean recycle = false;

	public void prepare() throws WriterException {
		this.recycle = false;
		this.druidPooledConnection = StaticData.getPooledConnectionByServerId(dataSyncRecord.getTargetServerId());
		this.commonRdbmsWriter.prepare(dataSyncRecord, taskConfig, this.druidPooledConnection.getConnection());
		CommonUtil.recycle(this.druidPooledConnection);
		this.recycle = true;
	}

	public void init(TaskConfig taskConfig, DataSyncRecord dataSyncRecord) {
		this.dataSyncRecord = dataSyncRecord;
		this.taskConfig = taskConfig;
		this.commonRdbmsWriter = new CommonRdbmsWriter(DATABASE_TYPE);
		this.path = taskConfig.getBcpDir() + File.separator + dataSyncRecord.getGuid() + ".bcp";
	}

	public void startWrite() throws WriterException {
		ServerConfigInfo serverConfigInfo = StaticData.getServerConfigInfo(dataSyncRecord.getTargetServerId());
		String dbLink = new StringBuilder("-S").append(serverConfigInfo.getUrl()).append(" -U").append(serverConfigInfo.getUsername())
				.append(" -P").append(serverConfigInfo.getPassword()).append(" -c").toString();
		String bcpCommand = new StringBuilder("bcp ")
				.append(StringUtils.isEmpty(dataSyncRecord.getCompleteTableBakName()) ? dataSyncRecord.getTableInfoTarget().getCompleteName()
						: dataSyncRecord.getCompleteTableBakName())
				.append(" in ").append(this.path).append(" ").append(dbLink).append(" -t\"|&\" -r\"|#\"")
				.toString();
		// 日志写入导入语句
		this.dataSyncRecord.getDataSyncLog().setBcpInScript(CommonUtil.desEncrypt(bcpCommand));
		List<String> list = CmdUtils.execBcpCommand(bcpCommand);
		if (!list.isEmpty()) {
			// 如果list 为空，则证明导出成功
			throw new WriterException(StringUtils.join(list, ","));
		}
	}

	public void post() throws WriterException {
		this.recycle = false;
		this.druidPooledConnection = StaticData.getPooledConnectionByServerId(dataSyncRecord.getTargetServerId());
		// 如果是复制的表到sync，则后续需要post操作，而且需要事务操作，删除和重命名需要一起执行
		if (StringUtils.isNotBlank(dataSyncRecord.getTableBakName())) {
			// 直接调用数据库存储过程进行删除重命名，避免因为网络延迟导致的删除执行后，重命名不执行的情况
			String dropRenameSql = String.format(SqlServerCommonSql.DROP_RENAME_PROC.getSql(), dataSyncRecord.getTargetDb(),
					dataSyncRecord.getTableBakName(),
					dataSyncRecord.getTargetTb(), dataSyncRecord.getTableBakName().replace(dataSyncRecord.getTargetTb(), ""));
			boolean executeResult = CommonCrud.execute(dropRenameSql, druidPooledConnection.getConnection());
			if (!executeResult) {
				throw new WriterException("执行存储过程将sync表rename为原表失败");
			}
		}
		CommonUtil.recycle(this.druidPooledConnection);
		this.recycle = true;
	}

	public void destroy() {

		try {
			// 还是手动关闭一下连接，避免因为上面出现异常就导致连接未正常关闭
			if (!recycle)
				CommonUtil.recycle(this.druidPooledConnection);
		} catch (Exception e) {
			//
		}
		try {
			FileUtils.forceDelete(new File(this.path));
		} catch (Exception e) {
			//
		}
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
