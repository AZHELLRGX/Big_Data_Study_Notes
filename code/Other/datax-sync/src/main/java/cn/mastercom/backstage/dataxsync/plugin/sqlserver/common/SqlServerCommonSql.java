package cn.mastercom.backstage.dataxsync.plugin.sqlserver.common;

// 关系型数据库的部分sql仍然不通用，需要区分
public enum SqlServerCommonSql {
	SELECT_O_ROW("select top 0 * from %s"),

	COPY_TABLE("EXEC MTNOH_AAA_DB.dbo.PROC_数据维护_表_复制_含索引 '%s', '%s', '%s', '%s'"),

	DROP_IF_EXISTS("if exists (select name from %s.sys.tables where name = '%s') drop table %s"),

	// 删除重命名改为调用数据库的存储过程
	DROP_RENAME_PROC("EXEC MTNOH_AAA_DB.[dbo].[PROC_数据维护_表_切换_rename方式] '%s', '%s', '%s', '%s'"),

	RENAME_TABLE("EXEC %s.sys.sp_rename '%s','%s'");

	private String sql;

	SqlServerCommonSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
