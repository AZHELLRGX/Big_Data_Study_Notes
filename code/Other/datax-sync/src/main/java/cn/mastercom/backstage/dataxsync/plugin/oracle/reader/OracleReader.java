package cn.mastercom.backstage.dataxsync.plugin.oracle.reader;

import cn.mastercom.backstage.dataxsync.core.plugin.Reader;
import org.apache.commons.lang3.StringUtils;

public class OracleReader extends Reader {

	@Override
	public void prepare() {
		// 这里如果没有指定sql，那么就视为select *(oracle是单实例，查询表不需要指定数据库名称)
		if (StringUtils.isEmpty(dataSyncRecord.getSqlStr())) {
			dataSyncRecord.setSqlStr("select * from " + dataSyncRecord.getSourceTb());
		}
	}
}
