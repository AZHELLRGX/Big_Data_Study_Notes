package cn.mastercom.backstage.dataxsync.util;

import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.mtcommon.crypto.Des;
import cn.mastercom.mtcommon.exceptions.CryptoException;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtil {

	private static final String IV = "12345678";
	private static final String KEY = "Chris   ";
	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	public static String desDecrypt(String data) {
		try {
			return Des.decrypt(data, CommonUtil.KEY, CommonUtil.IV, Des.ALGORITHM_CBC_PKCS5);
		} catch (CryptoException e) {
			return data;
		}
	}

	public static String desEncrypt(String data) {
		try {
			return Des.encrypt(data, CommonUtil.KEY, CommonUtil.IV, Des.ALGORITHM_CBC_PKCS5);
		} catch (CryptoException e) {
			return data;
		}
	}

	private CommonUtil() {
		//
	}

	/**
	 * 将数据处理成<Map<目标表名，Map<源表名，同步任务>>>的结构形式，避免多个线程处理同一个源表或者一个目标表导致数据库死锁
	 * 
	 * @param dataSyncRecords
	 * @return
	 */
	public static Map<String, Map<String, List<DataSyncRecord>>> handleDataSyncRecords(List<DataSyncRecord> dataSyncRecords) {
		Map<String, Map<String, List<DataSyncRecord>>> result = new HashMap<>();
		for (DataSyncRecord dataSyncRecord : dataSyncRecords) {
			String targetKey = dataSyncRecord.generateKeyTar();
			String sourceKey = dataSyncRecord.generateKeySou();
			if (result.containsKey(targetKey)) {
				Map<String, List<DataSyncRecord>> map = result.get(targetKey);
				if (map.containsKey(sourceKey)) {
					map.get(sourceKey).add(dataSyncRecord);
				} else {
					List<DataSyncRecord> dataSyncRecordList = new ArrayList<>();
					dataSyncRecordList.add(dataSyncRecord);
					map.put(sourceKey, dataSyncRecordList);
					result.put(targetKey, map);
				}
			} else {
				Map<String, List<DataSyncRecord>> map = new HashMap<>();
				List<DataSyncRecord> dataSyncRecordList = new ArrayList<>();
				dataSyncRecordList.add(dataSyncRecord);
				map.put(sourceKey, dataSyncRecordList);
				result.put(targetKey, map);
			}
		}
		return result;
	}

	// 获取数据库的完整表名称
	public static String getCompleteTableName(String dataType, String dbName, String tbName) {
		switch (dataType.toUpperCase()) {
		case "MYSQL":
			// 为了防止特殊表名，加上``
			return dbName + ".`" + tbName + "`";
		case "ORACLE":
			return tbName;
		case "SYBASEIQ":
			// 测试时用的 return new StringBuilder("[").append(dbName).append("].[DBA].[").append(tbName).append("]").toString()
		case "SQLSERVER":
		default:
			return getCompleteSqlServerTbName(dbName, tbName);
		}
	}

	private static String getCompleteSqlServerTbName(String dbName, String tbName) {
		return new StringBuilder("[").append(dbName).append("].[dbo].[").append(tbName).append("]").toString();
	}

	public static void recycle(DruidPooledConnection druidPooledConnection) {
		try {
			if (druidPooledConnection != null)
				druidPooledConnection.recycle();
		} catch (SQLException e) {
			//
			logger.error("数据库连接池回收出现错误", e);
		}
	}
}
