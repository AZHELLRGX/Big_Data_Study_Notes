package cn.mastercom.backstage.dataxsync.plugin.oracle.common;

public enum OracleCommonSql {
	SELECT_O_ROW("select * from %s where rownum <= 0"),

	// 目前这个复制表没有复制索引和主键
	COPY_TABLE("create table %s as (select * from %s where 1=2)"),

	COPY_IF_NOT_EXISTS("create table if not exists  %s as (select * from %s where 1=2)"),

	DROP_IF_EXISTS("declare num number;" +
			"begin" +
			"  select count(1) into num from user_tables where table_name = upper('%s') ;" +
			"  if num > 0 then execute immediate 'drop table %s' ;" +
			"  end if;" +
			"end;"),

	RENAME_TABLE("RENAME %s TO %s;");

	private String sql;

	OracleCommonSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
