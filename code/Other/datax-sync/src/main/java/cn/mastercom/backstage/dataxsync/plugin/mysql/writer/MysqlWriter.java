package cn.mastercom.backstage.dataxsync.plugin.mysql.writer;

import cn.mastercom.backstage.dataxsync.core.exception.WriterException;
import cn.mastercom.backstage.dataxsync.core.plugin.Writer;
import cn.mastercom.backstage.dataxsync.plugin.mysql.common.MysqlCommonSql;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import org.apache.commons.lang3.StringUtils;

public class MysqlWriter extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.MySql;

	public MysqlWriter() {
		this.dataBaseType = DATABASE_TYPE;
	}

	@Override
	public void post() throws WriterException {
		// 如果是复制的表到sync，则后续需要post操作
		// 可以走到这一步表示入库成功，接下来删除原表，把sync表rename为原表
		if (StringUtils.isNotBlank(dataSyncRecord.getTableBakName())) {
			String dropSql = String.format(MysqlCommonSql.DROP_IF_EXISTS.getSql(), dataSyncRecord.getTableInfoTarget().getCompleteName());

			String renameSql = String.format(MysqlCommonSql.RENAME_TABLE.getSql(), dataSyncRecord.getCompleteTableBakName(),
					dataSyncRecord.getTableInfoTarget().getCompleteName());

			this.commonRdbmsWriter.post(dropSql, renameSql, druidPooledConnection.getConnection());
		}
	}

}
