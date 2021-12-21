package cn.mastercom.backstage.dataxsync.plugin.sqlserver.reader;

import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.core.exception.ReaderException;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import cn.mastercom.backstage.dataxsync.util.CmdUtils;
import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SqlServerReaderBcp {
	private DataSyncRecord dataSyncRecord;

	private TaskConfig taskConfig;

	private String path;

	private String bcpCommand;

	public void init(TaskConfig taskConfig, DataSyncRecord dataSyncRecord) {
		this.taskConfig = taskConfig;
		this.dataSyncRecord = dataSyncRecord;
	}

	public void startRead() throws ReaderException {
		List<String> list = CmdUtils.execBcpCommand(this.bcpCommand);
		if (!list.isEmpty()) {
			// 如果list 为空，则证明导出成功
			throw new ReaderException(StringUtils.join(list, ","));
		}
	}

	public void prepare() {
		this.path = taskConfig.getBcpDir() + File.separator + dataSyncRecord.getGuid() + ".bcp";
		ServerConfigInfo serverConfigInfo = StaticData.getServerConfigInfo(dataSyncRecord.getSourceServerId());
		String dbLink = new StringBuilder("-S").append(serverConfigInfo.getUrl()).append(" -U").append(serverConfigInfo.getUsername())
				.append(" -P").append(serverConfigInfo.getPassword()).append(" -c").toString();
		// 这里如果没有指定sql，那么就视为select *
		if (StringUtils.isEmpty(dataSyncRecord.getSqlStr())) {
			this.bcpCommand = new StringBuilder("bcp ")
					.append(dataSyncRecord.getTableInfoSource().getCompleteName())
					.append(" out ")
					.append(this.path).append(" ").append(dbLink).append(" -t\"|&\" -r\"|#\"")
					.toString();
		} else {
			this.bcpCommand = new StringBuilder("bcp \"").append(dataSyncRecord.getSqlStr()).append("\" QUERYOUT ")
					.append(this.path).append(" ").append(dbLink).append(" -t\"|&\" -r\"|#\"")
					.toString();
		}
		this.dataSyncRecord.getDataSyncLog().setBcpOutScript(CommonUtil.desEncrypt(this.bcpCommand));
	}

	public void markFail(Throwable e) {
		// 记录日志到数据库，读取失败
		dataSyncRecord.setStatusId(5);
		dataSyncRecord.getDataSyncLog().setInfo(e.getMessage());
		dataSyncRecord.getDataSyncLog().setSuccess(false);
	}

	public void destroy() {
		try {
			FileUtils.forceDelete(new File(this.path));
		} catch (IOException e) {
			//
		}
	}
}
