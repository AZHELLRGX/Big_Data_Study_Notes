package cn.mastercom.backstage.dataxsync.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class WriterUtil {

	private WriterUtil() {
		//
	}

	public static String getWriteTemplate(List<String> columnHolders, List<String> valueHolders) {

		String writeDataSqlTemplate;

		writeDataSqlTemplate = "INSERT INTO %s (" + StringUtils.join(columnHolders, ",") + ") VALUES(" + StringUtils.join(valueHolders, ",")
				+ ")";

		return writeDataSqlTemplate;
	}

}
