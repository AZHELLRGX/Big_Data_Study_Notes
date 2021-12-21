package cn.mastercom.backstage.dataxsync.plugin.sqlserver.writer;

import cn.mastercom.backstage.dataxsync.core.exception.WriterException;
import cn.mastercom.backstage.dataxsync.core.plugin.Writer;
import cn.mastercom.backstage.dataxsync.plugin.sqlserver.common.SqlServerCommonSql;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import org.apache.commons.lang3.StringUtils;

public class SqlServerWriter extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.SQLServer;

	public SqlServerWriter() {
		this.dataBaseType = DATABASE_TYPE;
	}

	@Override
	public void post() throws WriterException {
		// 如果是复制的表到sync，则后续需要post操作，而且需要事务操作，删除和重命名需要一起执行
		if (StringUtils.isNotBlank(dataSyncRecord.getTableBakName())) {
			String dropSql = String.format(SqlServerCommonSql.DROP_IF_EXISTS.getSql(),
					dataSyncRecord.getTargetDb(),
					dataSyncRecord.getTargetTb(), dataSyncRecord.getTableInfoTarget().getCompleteName());

			String renameSql = String.format(SqlServerCommonSql.RENAME_TABLE.getSql(), dataSyncRecord.getTargetDb(),
					dataSyncRecord.getTableBakName(), dataSyncRecord.getTargetTb());
			this.commonRdbmsWriter.post(dropSql, renameSql, druidPooledConnection.getConnection());
		}
	}

}
