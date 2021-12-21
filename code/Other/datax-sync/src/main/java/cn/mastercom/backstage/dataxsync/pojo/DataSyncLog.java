package cn.mastercom.backstage.dataxsync.pojo;

import java.util.Date;

/**
 * 数据同步的信息日志记录
 */
public class DataSyncLog {

	private String guid;
	private Date beginTime;
	private Date endTime;
	private boolean success;
	private String info = "";
	// 备份导入导出命令，方便核查
	private String bcpInScript = ""; // 导入命令备份
	private String bcpOutScript = ""; // 导出命令备份

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getBcpInScript() {
		return bcpInScript;
	}

	public void setBcpInScript(String bcpInScript) {
		this.bcpInScript = bcpInScript;
	}

	public String getBcpOutScript() {
		return bcpOutScript;
	}

	public void setBcpOutScript(String bcpOutScript) {
		this.bcpOutScript = bcpOutScript;
	}
}
