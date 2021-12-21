package cn.mastercom.backstage.dataxsync.pojo;

import java.util.List;

public class TableInfo {
	/**
	 * 表的完整名称（主要用来进行同步）
	 */
	private String completeName;
	/**
	 * 表字段
	 */
	private List<String> columns;

	private List<Integer> columnTypes;

	// 表字段数量
	private int columnSize;

	// 表所在数据库类型
	private String dbType;

	// 带服务器ID的表的完整名称

	private String completeNameWithIp;

	public String getCompleteNameWithIp() {
		return completeNameWithIp;
	}

	public void setCompleteNameWithIp(String completeNameWithIp) {
		this.completeNameWithIp = completeNameWithIp;
	}

	public List<Integer> getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(List<Integer> columnTypes) {
		this.columnTypes = columnTypes;
	}

	public String getCompleteName() {
		return completeName;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
}
