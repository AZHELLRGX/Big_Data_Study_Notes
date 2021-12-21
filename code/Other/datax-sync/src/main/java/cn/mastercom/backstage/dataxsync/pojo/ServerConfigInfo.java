package cn.mastercom.backstage.dataxsync.pojo;

import cn.mastercom.backstage.dataxsync.util.CommonUtil;

/**
 * 为了安全起见，数据库连接信息均使用加密方式（连接串、用户名、密码）
 */
public class ServerConfigInfo {

	private int id;
	private String url;
	private String username;
	private String password;
	private String type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return CommonUtil.desDecrypt(url);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return CommonUtil.desDecrypt(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		// 需要解密
		return CommonUtil.desDecrypt(password);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
