package cn.mastercom.backstage.dataxsync.plugin.sybaseiq.common;

/**
 * @Author zhuwenchao
 * @Describe 关系型数据库的部分sql仍然不通用，需要区分
 */
public enum SybaseIqCommonSql {
	SELECT_O_ROW("select * from %s where 1>1");

	private String sql;

	SybaseIqCommonSql(String sql) {
		this.sql = sql;
	}

	public String getSql() {
		return sql;
	}
}
