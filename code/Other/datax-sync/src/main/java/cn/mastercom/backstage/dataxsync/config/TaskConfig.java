package cn.mastercom.backstage.dataxsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "setting")
public class TaskConfig {

	private String id;
	private boolean typeConversion;
	private int threadCount;

	private int fetchSize;
	// 下面批量写配置应该是自动计算的
	private int batchByteSize;

	private String bcpDir;

	public void setId(String id) {
		this.id = id;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public int getBatchByteSize() {
		return batchByteSize;
	}

	public void setBatchByteSize(int batchByteSize) {
		this.batchByteSize = batchByteSize;
	}

	public String getBcpDir() {
		return bcpDir;
	}

	public void setBcpDir(String bcpDir) {
		this.bcpDir = bcpDir;
	}

	public boolean isTypeConversion() {
		return typeConversion;
	}

	public void setTypeConversion(boolean typeConversion) {
		this.typeConversion = typeConversion;
	}

	private List<String> ids;

	public List<String> getIds() {
		if (ids == null) {
			ids = Arrays.asList(this.id.split(","));
		}
		return ids;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
}
