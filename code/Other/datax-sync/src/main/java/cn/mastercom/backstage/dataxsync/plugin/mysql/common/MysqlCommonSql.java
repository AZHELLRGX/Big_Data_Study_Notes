package cn.mastercom.backstage.dataxsync.plugin.mysql.common;

// 关系型数据库的部分sql仍然不通用，需要区分
public enum MysqlCommonSql {
	SELECT_O_ROW("select * from %s limit 0,0"),
	COPY_TABLE("create table %s like %s"),
	COPY_IF_NOT_EXISTS("create table if not exists %s like %s"),
	DROP_IF_EXISTS("drop table if exists %s"),
	RENAME_TABLE("rename table %s to %s");

	private String sql;

	MysqlCommonSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
