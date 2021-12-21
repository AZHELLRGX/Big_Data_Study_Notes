package cn.mastercom.backstage.dataxsync.dao;

import cn.mastercom.backstage.dataxsync.plugin.mysql.common.MysqlCommonSql;
import cn.mastercom.backstage.dataxsync.plugin.oracle.common.OracleCommonSql;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.common.SqlServerCommonSql;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * 负责操作地市库，地市库信息从main库的dbSetting获取
 */
public class CommonCrud {

	private CommonCrud() {
		//
	}

	private static final Logger log = LoggerFactory.getLogger(CommonCrud.class);

	/**
	 * 根据数据库类型复制表结构 复制场景出现在天表和没有delete条件的时候 应当在复制之前判断表是否存在，然后drop后再复制
	 *
	 * @param dbName
	 * @param dbType
	 * @param table
	 * @param copyName
	 * @param connection
	 */
	public static boolean copyTable(String dbName, String dbType, String table, String copyName, Connection connection) {
		String completeName = CommonUtil.getCompleteTableName(dbType, dbName, table);
		String completeCopyName = CommonUtil.getCompleteTableName(dbType, dbName, copyName);
		String dropSql;
		String copySql;
		boolean isOk;
		switch (dbType.toUpperCase()) {
		case "MYSQL":
			// 先判断复制的表是否存在，存在则删除
			dropSql = String.format(MysqlCommonSql.DROP_IF_EXISTS.getSql(), completeCopyName);
			isOk = execute(dropSql, connection);
			if (isOk) {
				copySql = String.format(MysqlCommonSql.COPY_TABLE.getSql(), completeCopyName, completeName);
				isOk = execute(copySql, connection);
			}
			return isOk;
		case "SQLSERVER":
			dropSql = String.format(SqlServerCommonSql.DROP_IF_EXISTS.getSql(), dbName, copyName, completeCopyName);

			isOk = execute(dropSql, connection);
			if (isOk) {
				copySql = String.format(SqlServerCommonSql.COPY_TABLE.getSql(), dbName, table, dbName, copyName);
				isOk = execute(copySql, connection);
			}
			return isOk;
		case "ORACLE":
			dropSql = String.format(OracleCommonSql.DROP_IF_EXISTS.getSql(), copyName, copyName);
			isOk = execute(dropSql, connection);
			if (isOk) {
				copySql = String.format(OracleCommonSql.COPY_TABLE.getSql(), copyName, table);
				isOk = execute(copySql, connection);
			}
			return isOk;
		default:
			return false;
		}
	}

	/**
	 * @param sql
	 * @param conn
	 */
	public static boolean execute(String sql, Connection conn) {
		Statement statement = null;
		try {
			conn.setAutoCommit(true); // 这个连接可能被fetchSize查询使用过
			statement = conn.createStatement();
			statement.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			log.error("执行{}语句错误", sql, e);
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					log.error("statement关闭错误", e);
				}
			}
		}
	}

	public static boolean executeBatch(List<String> sqls, Connection conn) {
		Statement statement = null;
		try {
			conn.setAutoCommit(true);
			statement = conn.createStatement();
			for (String sql : sqls) {
				statement.addBatch(sql);
			}
			statement.executeBatch();
			statement.clearBatch();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					log.error("statement关闭错误", e);
				}
			}
		}
	}

	/**
	 * 根据数据库类型复制表结构 复制场景出现在天表和没有delete条件的时候 应当在复制之前判断表是否存在，然后drop后再复制
	 *
	 * @param dbName
	 * @param dbType
	 * @param table
	 * @param copyName
	 * @param connection
	 */
	public static boolean copyTableIfNotExists(String dbName, String dbType, String table, String copyName, Connection connection) {
		String completeName = CommonUtil.getCompleteTableName(dbType, dbName, table);
		String completeCopyName = CommonUtil.getCompleteTableName(dbType, dbName, copyName);
		String copySql;
		boolean isOk;
		switch (dbType.toUpperCase()) {
			case "MYSQL":
				copySql = String.format(MysqlCommonSql.COPY_IF_NOT_EXISTS.getSql(), completeCopyName, completeName);
				isOk = execute(copySql, connection);
				return isOk;
			case "SQLSERVER":
				copySql = String.format(SqlServerCommonSql.COPY_TABLE.getSql(), dbName, table, dbName, copyName);
				isOk = execute(copySql, connection);
				return isOk;
			case "ORACLE":
				copySql = String.format(OracleCommonSql.COPY_IF_NOT_EXISTS.getSql(), copyName, table);
				isOk = execute(copySql, connection);
				return isOk;
			default:
				return false;
		}
	}

}
