package cn.mastercom.backstage.dataxsync.plugin.mysql.reader;

import cn.mastercom.backstage.dataxsync.core.plugin.Reader;
import org.apache.commons.lang3.StringUtils;

public class MysqlReader extends Reader {

	@Override
	public void prepare() {
		// 这里如果没有指定sql，那么就视为select *
		if (StringUtils.isEmpty(dataSyncRecord.getSqlStr())) {
			dataSyncRecord.setSqlStr("select * from " + dataSyncRecord.getTableInfoSource().getCompleteName());
		}
	}
}
