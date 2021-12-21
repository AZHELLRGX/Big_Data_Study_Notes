package cn.mastercom.backstage.dataxsync.core.rdbms;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.transport.RecordSender;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import com.alibaba.datax.common.element.*;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CommonRdbmsReader {

	private static final Logger LOG = LoggerFactory.getLogger(CommonRdbmsReader.class);
	private static final boolean IS_DEBUG = LOG.isDebugEnabled();

	private int fetchSize;

	public CommonRdbmsReader(TaskConfig taskConfig) {
		this.fetchSize = taskConfig.getFetchSize();
	}

	public void startRead(DataSyncRecord dataSyncRecord, RecordSender<Record> recordSender, Connection conn) {
		String querySql = dataSyncRecord.getSqlStr();

		ResultSet rs = null;
		try {
			rs = DBUtil.query(conn, querySql, fetchSize);

			ResultSetMetaData metaData = rs.getMetaData();

			// 需要转码
			while (rs.next()) {
				this.transportOneRecord(recordSender, rs, metaData);
			}
			// 必须加上这段代码，否则内存同步数据会出现脏读
			conn.commit();
			LOG.info("Finished read record by Sql: [{}].", querySql);

		} catch (Exception e) {
			LOG.error("startRead error{}", dataSyncRecord, e);
		} finally {
			DBUtil.closeResultSet(rs);
		}
	}

	protected Record transportOneRecord(RecordSender<Record> recordSender, ResultSet rs, ResultSetMetaData metaData) {
		Record record = buildRecord(recordSender, rs, metaData);
		recordSender.sendToWriter(record);
		return record;
	}

	protected Record buildRecord(RecordSender<Record> recordSender, ResultSet rs, ResultSetMetaData metaData) {
		Record record = recordSender.createRecord();
		try {
			int columnNumber = metaData.getColumnCount();
			for (int i = 1; i <= columnNumber; i++) {
				switch (metaData.getColumnType(i)) {

				case Types.CHAR:
				case Types.NCHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
				case Types.NVARCHAR:
				case Types.LONGNVARCHAR:
					String rawData;
					rawData = rs.getString(i);
					record.addColumn(new StringColumn(rawData));
					break;

				case Types.CLOB:
				case Types.NCLOB:
					record.addColumn(new StringColumn(rs.getString(i)));
					break;

				case Types.SMALLINT:
				case Types.TINYINT:
				case Types.INTEGER:
				case Types.BIGINT:
					record.addColumn(new LongColumn(rs.getString(i)));
					break;

				case Types.NUMERIC:
				case Types.DECIMAL:
					record.addColumn(new DoubleColumn(rs.getString(i)));
					break;

				case Types.FLOAT:
				case Types.REAL:
				case Types.DOUBLE:
					record.addColumn(new DoubleColumn(rs.getString(i)));
					break;

				case Types.TIME:
					record.addColumn(new DateColumn(rs.getTime(i)));
					break;

				// for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
				case Types.DATE:
					if (metaData.getColumnTypeName(i).equalsIgnoreCase("year")) {
						record.addColumn(new LongColumn(rs.getInt(i)));
					} else {
						record.addColumn(new DateColumn(rs.getDate(i)));
					}
					break;

				case Types.TIMESTAMP:
					record.addColumn(new DateColumn(rs.getTimestamp(i)));
					break;

				case Types.BINARY:
				case Types.VARBINARY:
				case Types.BLOB:
				case Types.LONGVARBINARY:
					record.addColumn(new BytesColumn(rs.getBytes(i)));
					break;

				// warn: bit(1) -> Types.BIT 可使用BoolColumn
				// warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
				case Types.BOOLEAN:
				case Types.BIT:
					record.addColumn(new BoolColumn(rs.getBoolean(i)));
					break;

				case Types.NULL:
					String stringData = null;
					if (rs.getObject(i) != null) {
						stringData = rs.getObject(i).toString();
					}
					record.addColumn(new StringColumn(stringData));
					break;

				default:
					throw DataXException
							.asDataXException(
									DBUtilErrorCode.UNSUPPORTED_TYPE,
									String.format(
											"您的配置文件中的列配置信息有误. 因为DataX 不支持数据库读取这种字段类型. 字段名:[%s], 字段名称:[%s], 字段Java类型:[%s]. 请尝试使用数据库函数将其转换datax支持的类型 或者不同步该字段 .",
											metaData.getColumnName(i),
											metaData.getColumnType(i),
											metaData.getColumnClassName(i)));
				}
			}
		} catch (Exception e) {
			if (IS_DEBUG) {
				LOG.debug("read data {} occur exception:", record, e);
			}
		}
		return record;
	}
}
