package cn.mastercom.backstage.dataxsync.core.rdbms;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.WriterException;
import cn.mastercom.backstage.dataxsync.core.transport.RecordReceiver;
import cn.mastercom.backstage.dataxsync.dao.CommonCrud;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.TableInfo;
import cn.mastercom.backstage.dataxsync.util.WriterUtil;
import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommonRdbmsWriter {
	// mysql批量插入的时候，应该
	private static final int M_N = 65535;
	protected static final Logger LOG = LoggerFactory
			.getLogger(CommonRdbmsWriter.class);

	protected DataBaseType dataBaseType;
	private static final String VALUE_HOLDER = "?";
	private String table;
	protected List<String> columns;
	protected int batchSize;
	protected int batchByteSize;
	protected int columnNumber = 0;

	protected String writeRecordSql;
	protected boolean emptyAsNull;
	protected Triple<List<String>, List<Integer>, List<String>> resultSetMetaData;

	public CommonRdbmsWriter(DataBaseType dataBaseType) {
		this.dataBaseType = dataBaseType;
	}

	public void prepare(DataSyncRecord dataSyncRecord, TaskConfig taskConfig, Connection connection) throws WriterException {
		TableInfo tableInfo = dataSyncRecord.getTableInfoTarget();
		this.table = StringUtils.isEmpty(dataSyncRecord.getCompleteTableBakName()) ? tableInfo.getCompleteName()
				: dataSyncRecord.getCompleteTableBakName();
		this.columns = tableInfo.getColumns();
		// 谨记：如果同步记录有select语句，由于不会检测源数据与目标表字段的对应关系，很可能因为字段对不上导致空指针异常，目前还没想好怎么改，以观后效
		this.columnNumber = tableInfo.getColumnSize();
		this.batchSize = M_N / columnNumber;
		this.batchByteSize = taskConfig.getBatchByteSize();

		//是否需要分表
		if (dataSyncRecord.isCutTb()) {
			boolean copyResult;
			if (!StringUtils.isEmpty(dataSyncRecord.getDeleteCon())) {
				copyResult = CommonCrud.copyTableIfNotExists(dataSyncRecord.getTargetDb(),
						dataSyncRecord.getTableInfoTarget().getDbType(),
						dataSyncRecord.getModelTb(),
						dataSyncRecord.getTargetTb(),
						connection);
			} else {
				copyResult = CommonCrud.copyTable(dataSyncRecord.getTargetDb(),
						dataSyncRecord.getTableInfoTarget().getDbType(),
						dataSyncRecord.getModelTb(),
						dataSyncRecord.getTargetTb(),
						connection);
			}
			if (!copyResult) {
				throw new WriterException("复制天表失败");
			}
		}
		//是否有删除条件
		if (!StringUtils.isEmpty(dataSyncRecord.getDeleteCon())) {
			boolean executeResult = CommonCrud.execute(dataSyncRecord.getDeleteCon(), connection);
			if (!executeResult) {
				throw new WriterException("执行delete条件失败");
			}
		}
		// 不分表且没有删除条件
		if (!dataSyncRecord.isCutTb() && StringUtils.isEmpty(dataSyncRecord.getDeleteCon())) {
			boolean copyResult = CommonCrud.copyTable(dataSyncRecord.getTargetDb(),
					dataSyncRecord.getTableInfoTarget().getDbType(),
					dataSyncRecord.getTargetTb(),
					dataSyncRecord.getTableBakName(),
					connection);
			if (!copyResult) {
				throw new WriterException("复制原表为sync表失败");
			}
		}
	}

	public void post(String dropSql, String renameSql, Connection connection) throws WriterException {
		List<String> sqlList = new ArrayList<>();
		sqlList.add(dropSql);
		sqlList.add(renameSql);
		boolean executeResult = CommonCrud.executeBatch(sqlList, connection);

		if (!executeResult) {
			throw new WriterException("执行drop原表以及将sync表rename为原表失败");
		}
	}

	public void startWriteWithConnection(RecordReceiver<Record> recordReceiver, Connection connection)
			throws WriterException {
		// 用于写入数据的时候的类型根据目的表字段类型转换
		try {
			String columnString = StringUtils.join(this.columns, ",");
			this.resultSetMetaData = DBUtil.getColumnMetaData(connection,
					this.table, columnString);
		} catch (Exception e) {
			// 将任务错误信息处理的简单一些
			LOG.error("ERROR:获取表{}字段信息异常,{}", this.table, e.getMessage());
			throw new WriterException("ERROR:获取目标表字段信息异常");
		}

		// 写数据库的SQL语句
		calcWriteRecordSql();

		List<Record> writeBuffer = new ArrayList<>(this.batchSize);
		int bufferBytes = 0;
		try {
			Record record;
			while ((record = recordReceiver.getFromReader()) != null) {

				writeBuffer.add(record);
				bufferBytes += record.getMemorySize();

				if (writeBuffer.size() >= batchSize || bufferBytes >= batchByteSize) {
					doBatchInsert(connection, writeBuffer);
					writeBuffer.clear();
					bufferBytes = 0;
				}
			}
			if (!writeBuffer.isEmpty()) {
				doBatchInsert(connection, writeBuffer);
				writeBuffer.clear();
			}
		} catch (Exception e) {
			throw new WriterException("startWriteWithConnection error");
		} finally {
			writeBuffer.clear();
		}
	}

	public void startWrite(RecordReceiver<Record> recordReceiver, Connection connection) throws WriterException {
		startWriteWithConnection(recordReceiver, connection);
	}

	protected void doBatchInsert(Connection connection, List<Record> buffer)
			throws SQLException {
		PreparedStatement preparedStatement = null;
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection
					.prepareStatement(this.writeRecordSql);

			for (Record record : buffer) {
				preparedStatement = fillPreparedStatement(
						preparedStatement, record);
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			connection.commit();
		} catch (SQLException e) {
			LOG.warn("回滚此次写入, 采用每次写入一行方式提交. 因为:{}", e.getMessage());
			connection.rollback();
			doOneInsert(connection, buffer);
		} catch (Exception e) {
			throw DataXException.asDataXException(
					DBUtilErrorCode.WRITE_DATA_ERROR, e);
		} finally {
			DBUtil.closeDBResources(preparedStatement, null);
		}
	}

	/**
	 * 批量插入退变为单条插入
	 * 
	 * @param connection
	 * @param buffer
	 */
	protected void doOneInsert(Connection connection, List<Record> buffer) {
		PreparedStatement preparedStatement = null;
		try {
			connection.setAutoCommit(true);
			preparedStatement = connection
					.prepareStatement(this.writeRecordSql);

			for (Record record : buffer) {
				insertOneRecord(preparedStatement, record);
			}
		} catch (Exception e) {
			throw DataXException.asDataXException(
					DBUtilErrorCode.WRITE_DATA_ERROR, e);
		} finally {
			DBUtil.closeDBResources(preparedStatement, null);
		}
	}

	/**
	 * 插入一条数据
	 * 
	 * @param preparedStatement
	 * @param record
	 * @throws SQLException
	 */
	public void insertOneRecord(PreparedStatement preparedStatement, Record record) throws SQLException {
		try {
			preparedStatement = fillPreparedStatement(
					preparedStatement, record);
			preparedStatement.execute();
		} catch (SQLException e) {
			LOG.debug(e.toString());
		} finally {
			// 最后不要忘了关闭 preparedStatement
			preparedStatement.clearParameters();
		}
	}

	// 直接使用了两个类变量：columnNumber,resultSetMetaData
	protected PreparedStatement fillPreparedStatement(PreparedStatement preparedStatement, Record record)
			throws SQLException {
		for (int i = 0; i < this.columnNumber; i++) {
			int columnSqltype = this.resultSetMetaData.getMiddle().get(i);
			preparedStatement = fillPreparedStatementColumnType(preparedStatement, i, columnSqltype, record.getColumn(i));
		}

		return preparedStatement;
	}

	/**
	 * 转换不同数据库之间的数据类型
	 * 
	 * @param preparedStatement
	 * @param columnIndex
	 * @param columnSqltype
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("all")
	protected PreparedStatement fillPreparedStatementColumnType(PreparedStatement preparedStatement, int columnIndex, int columnSqltype,
			Column column) throws SQLException {
		java.util.Date utilDate;
		switch (columnSqltype) {
		case Types.CHAR:
		case Types.NCHAR:
		case Types.CLOB:
		case Types.NCLOB:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
		case Types.LONGNVARCHAR:
			preparedStatement.setString(columnIndex + 1, column
					.asString());
			break;

		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
			String strValue = column.asString();
			if (emptyAsNull && "".equals(strValue)) {
				preparedStatement.setString(columnIndex + 1, null);
			} else {
				preparedStatement.setString(columnIndex + 1, strValue);
			}
			break;

		case Types.TINYINT:
			Long longValue = column.asLong();
			if (null == longValue) {
				preparedStatement.setString(columnIndex + 1, null);
			} else {
				preparedStatement.setString(columnIndex + 1, longValue.toString());
			}
			break;

		// for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
		case Types.DATE:
			if (this.resultSetMetaData.getRight().get(columnIndex)
					.equalsIgnoreCase("year")) {
				if (column.asBigInteger() == null) {
					preparedStatement.setString(columnIndex + 1, null);
				} else {
					preparedStatement.setInt(columnIndex + 1, column.asBigInteger().intValue());
				}
			} else {
				java.sql.Date sqlDate = null;
				try {
					utilDate = column.asDate();
				} catch (DataXException e) {
					throw new SQLException(String.format(
							"Date 类型转换错误：[%s]", column));
				}

				if (null != utilDate) {
					sqlDate = new java.sql.Date(utilDate.getTime());
				}
				preparedStatement.setDate(columnIndex + 1, sqlDate);
			}
			break;

		case Types.TIME:
			java.sql.Time sqlTime = null;
			try {
				utilDate = column.asDate();
			} catch (DataXException e) {
				throw new SQLException(String.format(
						"TIME 类型转换错误：[%s]", column));
			}

			if (null != utilDate) {
				sqlTime = new java.sql.Time(utilDate.getTime());
			}
			preparedStatement.setTime(columnIndex + 1, sqlTime);
			break;

		case Types.TIMESTAMP:
			java.sql.Timestamp sqlTimestamp = null;
			try {
				utilDate = column.asDate();
			} catch (DataXException e) {
				throw new SQLException(String.format(
						"TIMESTAMP 类型转换错误：[%s]", column));
			}

			if (null != utilDate) {
				sqlTimestamp = new java.sql.Timestamp(
						utilDate.getTime());
			}
			preparedStatement.setTimestamp(columnIndex + 1, sqlTimestamp);
			break;

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.BLOB:
		case Types.LONGVARBINARY:
			preparedStatement.setBytes(columnIndex + 1, column
					.asBytes());
			break;

		case Types.BOOLEAN:
			preparedStatement.setString(columnIndex + 1, column.asString());
			break;

		// warn: bit(1) -> Types.BIT 可使用setBoolean
		// warn: bit(>1) -> Types.VARBINARY 可使用setBytes
		case Types.BIT:
			if (this.dataBaseType == DataBaseType.MySql) {
				preparedStatement.setBoolean(columnIndex + 1, column.asBoolean());
			} else {
				preparedStatement.setString(columnIndex + 1, column.asString());
			}
			break;
		default:
			throw DataXException
					.asDataXException(
							DBUtilErrorCode.UNSUPPORTED_TYPE,
							String.format(
									"您的配置文件中的列配置信息有误. 因为DataX 不支持数据库写入这种字段类型. 字段名:[%s], 字段类型:[%d], 字段Java类型:[%s]. 请修改表中该字段的类型或者不同步该字段.",
									this.resultSetMetaData.getLeft()
											.get(columnIndex),
									this.resultSetMetaData.getMiddle()
											.get(columnIndex),
									this.resultSetMetaData.getRight()
											.get(columnIndex)));
		}
		return preparedStatement;
	}

	private void calcWriteRecordSql() {
		List<String> valueHolders = new ArrayList<>(columnNumber);
		for (int i = 0; i < columns.size(); i++) {
			valueHolders.add(calcValueHolder());
		}
		writeRecordSql = String.format(WriterUtil.getWriteTemplate(columns, valueHolders), this.table);
	}

	protected String calcValueHolder() {
		return VALUE_HOLDER;
	}

}
