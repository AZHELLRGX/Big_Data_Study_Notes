package cn.mastercom.backstage.dataxsync.pojo;

import cn.mastercom.backstage.dataxsync.core.exception.DataSyncRecordHandleException;
import cn.mastercom.backstage.dataxsync.plugin.mysql.common.MysqlCommonSql;
import cn.mastercom.backstage.dataxsync.plugin.oracle.common.OracleCommonSql;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.common.SqlServerCommonSql;
import cn.mastercom.backstage.dataxsync.plugin.sybaseiq.common.SybaseIqCommonSql;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataSyncRecord {
	private static final Logger LOG = LoggerFactory.getLogger(DataSyncRecord.class);

	private String guid;
	private Date dataTime;
	private int sourceServerId;
	private String sourceDb;
	private String sourceTb;
	private String sqlStr;
	private int targetServerId;
	private String targetDb;
	private String targetTb;
	private String deleteCon;
	private boolean cutTb;
	private String modelTb;
	private int statusId;
	private int taskId;
	/**
	 * 同步方式是否为bcp方式（sqlserver数据库之间的同步大数据量的表时候使用bcp可能合适一些）
	 */
	private boolean bcp;

	private String dateFormat;

	private String tableBakName;

	private String completeTableBakName;

	// 源数据表信息
	private TableInfo tableInfoSource;

	// 目标数据库信息
	private TableInfo tableInfoTarget;

	/**
	 * 数据同步日志
	 */
	private DataSyncLog dataSyncLog = new DataSyncLog();

	public boolean isBcp() {
		return bcp;
	}

	public void setBcp(boolean bcp) {
		this.bcp = bcp;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public DataSyncLog getDataSyncLog() {
		return dataSyncLog;
	}

	public void setDataSyncLog(DataSyncLog dataSyncLog) {
		this.dataSyncLog = dataSyncLog;
	}

	public String getCompleteTableBakName() {
		return completeTableBakName;
	}

	public void setCompleteTableBakName(String completeTableBakName) {
		this.completeTableBakName = completeTableBakName;
	}

	public String getTableBakName() {
		return tableBakName;
	}

	public void setTableBakName(String tableBakName) {
		this.tableBakName = tableBakName;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public int getSourceServerId() {
		return sourceServerId;
	}

	public void setSourceServerId(int sourceServerId) {
		this.sourceServerId = sourceServerId;
	}

	public String getSourceDb() {
		return sourceDb;
	}

	public void setSourceDb(String sourceDb) {
		this.sourceDb = sourceDb;
	}

	public String getSourceTb() {
		return sourceTb;
	}

	public void setSourceTb(String sourceTb) {
		this.sourceTb = sourceTb;
	}

	public String getSqlStr() {
		return sqlStr;
	}

	public void setSqlStr(String sqlStr) {
		this.sqlStr = sqlStr;
	}

	public int getTargetServerId() {
		return targetServerId;
	}

	public void setTargetServerId(int targetServerId) {
		this.targetServerId = targetServerId;
	}

	public String getTargetDb() {
		return targetDb;
	}

	public void setTargetDb(String targetDb) {
		this.targetDb = targetDb;
	}

	public String getTargetTb() {
		return targetTb;
	}

	public void setTargetTb(String targetTb) {
		this.targetTb = targetTb;
	}

	public String getDeleteCon() {
		return deleteCon;
	}

	public void setDeleteCon(String deleteCon) {
		this.deleteCon = deleteCon;
	}

	public boolean isCutTb() {
		return cutTb;
	}

	public void setCutTb(boolean cutTb) {
		this.cutTb = cutTb;
	}

	public String getModelTb() {
		return modelTb;
	}

	public void setModelTb(String modelTb) {
		this.modelTb = modelTb;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String generateKeyTar() {
		return targetServerId + "_" + targetDb + "_" + targetTb;
	}

	public String generateKeySou() {
		return sourceServerId + "_" + sourceDb + "_" + sourceTb;
	}

	public TableInfo getTableInfoSource() {
		return tableInfoSource;
	}

	public void setTableInfoSource(TableInfo tableInfoSource) {
		this.tableInfoSource = tableInfoSource;
	}

	public TableInfo getTableInfoTarget() {
		return tableInfoTarget;
	}

	public void setTableInfoTarget(TableInfo tableInfoTarget) {
		this.tableInfoTarget = tableInfoTarget;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		return new StringBuilder("from [").append(sourceServerId)
				.append(",").append(sourceDb)
				.append(",").append(sourceTb)
				.append("] to [")
				.append(targetServerId)
				.append(",").append(targetDb)
				.append(",").append(targetTb)
				.append("]").toString();
	}

	/**
	 *
	 * @param source 源数据库信息
	 * @param target 目标数据库信息
	 * @throws Exception
	 */
	public void init(ServerConfigInfo source, ServerConfigInfo target) throws DataSyncRecordHandleException {
		// 初始化源表和目标表信息
		try {
			this.tableInfoSource = getTableInfo(source, this.sourceDb, this.sourceTb, false);
			// 目标表注意：获取表结构的时候天表还不存在，需要使用模版表对比
			this.tableInfoTarget = getTableInfo(target, this.targetDb, this.targetTb, true);
		} catch (Exception e) {
			throw new DataSyncRecordHandleException(e.getMessage());
		}

		// 如果不分表且没有delete条件，则需要备份表
		if (!this.cutTb && StringUtils.isEmpty(this.deleteCon)) {

			if (DataBaseType.Oracle.getTypeName().equalsIgnoreCase(this.tableInfoTarget.getDbType())) {
				// oracle的表特殊处理一下
				this.tableBakName = this.targetTb + "_sync_" + this.guid.hashCode();
				this.completeTableBakName = this.tableBakName;
			}else if(DataBaseType.MySql.getTypeName().equalsIgnoreCase(this.tableInfoTarget.getDbType())){
				this.tableBakName = this.targetTb + "_sync";
				this.completeTableBakName = CommonUtil.getCompleteTableName(this.tableInfoTarget.getDbType(),
						this.targetDb, this.tableBakName);
			}else {
				// 复制为_sync有一个问题影响，那就是SqlServer 表更名时 ，索引名称不改变，导致下次sync新建时 冲突
				// replace guid to -> System.currentTimeMillis() + this.taskId
				this.tableBakName = this.targetTb + "_sync_" + System.currentTimeMillis() + this.taskId;
				this.completeTableBakName = CommonUtil.getCompleteTableName(this.tableInfoTarget.getDbType(),
						this.targetDb, this.tableBakName);
			}
		}

		// 如果deleteCon不带有delete from语句，需要自己拼接
		if (StringUtils.isNotBlank(this.deleteCon) && (!this.deleteCon.contains("delete") && !this.deleteCon.contains("DELETE"))) {
			this.deleteCon = "delete from " + this.tableInfoTarget.getCompleteName() + " where " + this.deleteCon;
		}
	}

	/**
	 * 获取表相关信息（表结构、全限定名）
	 * 
	 * @param serverConfigInfo
	 * @param dbName
	 * @param tbName
	 * @param target
	 * @return
	 * @throws DataSyncRecordHandleException
	 */
	private TableInfo getTableInfo(ServerConfigInfo serverConfigInfo, String dbName, String tbName, boolean target)
			throws DataSyncRecordHandleException {
		DruidPooledConnection druidPooledConnection = null;
		TableInfo tableInfo = new TableInfo();
		tableInfo.setDbType(serverConfigInfo.getType());
		ResultSet rs = null;
		String querySql = null;
		try {
			// 将获取连接的步骤放在里面执行，避免因为数据库连接配置有误导致异常无法被捕获
			druidPooledConnection = StaticData.getPooledConnectionByServerId(serverConfigInfo.getId());
			Connection connection = druidPooledConnection.getConnection();
			tableInfo.setCompleteName(CommonUtil.getCompleteTableName(serverConfigInfo.getType(), dbName, tbName));
			tableInfo.setCompleteNameWithIp(serverConfigInfo.getId() + "-" + tableInfo.getCompleteName());

			querySql = calQuerySql(serverConfigInfo, dbName, tableInfo, target);

			List<String> columns = new ArrayList<>();
			List<Integer> columnTypes = new ArrayList<>();
			rs = DBUtil.query(connection, querySql);
			ResultSetMetaData metaData = rs.getMetaData();

			switch (serverConfigInfo.getType().toUpperCase()) {
			case "MYSQL":
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					columns.add("`" + metaData.getColumnName(i) + "`");
					columnTypes.add(metaData.getColumnType(i));
				}
				break;
			case "SYBASEIQ":
			case "SQLSERVER":
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					columns.add("[" + metaData.getColumnName(i) + "]");
					columnTypes.add(metaData.getColumnType(i));
				}
				break;
			default:
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					columns.add(metaData.getColumnName(i));
					columnTypes.add(metaData.getColumnType(i));
				}
				break;
			}
			tableInfo.setColumns(columns);
			tableInfo.setColumnTypes(columnTypes);
			tableInfo.setColumnSize(columns.size());
		} catch (Exception e) {
			LOG.error("SQL:{}", querySql);
			throw new DataSyncRecordHandleException(e.getMessage());
		} finally {
			DBUtil.closeResultSet(rs);
			CommonUtil.recycle(druidPooledConnection);
		}
		return tableInfo;
	}

	/**
	 * 根据表信息拼接查询sql
	 * 
	 * @param serverConfigInfo
	 * @param dbName
	 * @param tableInfo
	 * @param target
	 * @return
	 * @throws DataSyncRecordHandleException
	 */
	@SuppressWarnings("all")
	public String calQuerySql(ServerConfigInfo serverConfigInfo, String dbName, TableInfo tableInfo, boolean target)
			throws DataSyncRecordHandleException {
		String querySql;
		switch (serverConfigInfo.getType().toUpperCase()) {
		case "MYSQL":
			if (!target && StringUtils.isNotBlank(this.getSqlStr())
					&& !StringUtils.contains(StringUtils.deleteWhitespace(this.getSqlStr().toUpperCase()), "SELECT*")) {

				querySql = this.getSqlStr() + " limit 0,0";
			} else if (target && this.cutTb) {
				querySql = String.format(MysqlCommonSql.SELECT_O_ROW.getSql(),
						CommonUtil.getCompleteTableName(serverConfigInfo.getType(), dbName, this.modelTb));
			} else {
				querySql = String.format(MysqlCommonSql.SELECT_O_ROW.getSql(), tableInfo.getCompleteName());
			}
			break;
		case "SQLSERVER":
			if (!target && StringUtils.isNotBlank(this.getSqlStr())
					&& !StringUtils.contains(StringUtils.deleteWhitespace(this.getSqlStr().toUpperCase()), "SELECT*")) {

				int index = sqlStr.trim().toLowerCase().indexOf("select");
				if (index != 0) {
					throw new DataSyncRecordHandleException("SQL语句 不是以select 开头");
				}
				querySql = "select top 0" + sqlStr.trim().substring(6);
			} else if (target && this.cutTb) {
				// 如果表是目标表而且是天表
				querySql = String.format(SqlServerCommonSql.SELECT_O_ROW.getSql(),
						CommonUtil.getCompleteTableName(serverConfigInfo.getType(), dbName, this.modelTb));
			} else {
				querySql = String.format(SqlServerCommonSql.SELECT_O_ROW.getSql(), tableInfo.getCompleteName());
			}
			break;
		case "ORACLE":
			if (target && this.cutTb) {
				// 如果表是目标表而且是天表
				querySql = String.format(OracleCommonSql.SELECT_O_ROW.getSql(), this.modelTb);
			} else {
				querySql = String.format(OracleCommonSql.SELECT_O_ROW.getSql(), tableInfo.getCompleteName());
			}
			break;
		case "SYBASEIQ":
			if (!target && StringUtils.isNotBlank(this.getSqlStr())
					&& !StringUtils.contains(StringUtils.deleteWhitespace(this.getSqlStr().toUpperCase()), "SELECT*")) {

				int index = sqlStr.trim().toLowerCase().indexOf("select");
				if (index != 0) {
					throw new DataSyncRecordHandleException("SQL语句 不是以select 开头");
				}
				int whereIndex = sqlStr.trim().toLowerCase().indexOf("where");
				if(whereIndex < 0) {
					querySql = sqlStr.trim() + " where 1 > 1";
				} else {
					querySql = sqlStr.trim().substring(0,whereIndex + 5) + " (" + sqlStr.trim().substring(whereIndex + 5) + " ) and 1 > 1";
				}
			} else if (target && this.cutTb) {
				// 如果表是目标表而且是天表
				querySql = String.format(SybaseIqCommonSql.SELECT_O_ROW.getSql(),
						CommonUtil.getCompleteTableName(serverConfigInfo.getType(), dbName, this.modelTb));
			} else {
				querySql = String.format(SybaseIqCommonSql.SELECT_O_ROW.getSql(), tableInfo.getCompleteName());
			}
			break;
		default:
			throw new DataSyncRecordHandleException("数据库连接配置出错，不存在的数据库类型");
		}
		return querySql;
	}
}
