package cn.mastercom.backstage.dataxsync.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CmdUtils {
	private static final Logger log = LoggerFactory.getLogger(CmdUtils.class);

	private CmdUtils() {
		// do nothing
	}

	public static List<String> execBcpCommand(String cmd) {
		List<String> list = new ArrayList<>();
		String charset;
		String[] cmdA;
		String osName = System.getProperty("os.name");
		if ("Linux".equals(osName)) {
			cmdA = new String[] { "/bin/sh", "-c", cmd };
			charset = "UTF-8";
		} else {
			cmdA = new String[] { "cmd", "/c", cmd };
			charset = "GBK";
		}
		Process process;
		try {
			process = Runtime.getRuntime().exec(cmdA);
		} catch (IOException e) {
			return list;
		}
		try (InputStream inputStream = process.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
			// 这里需要注意的是windows服务器的默认编码是GBK，但是Linux的默认编码则是UTF-8
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// SQLState = S1000, NativeError = 0的日志只能算是警告，不算是错误
				if (!"".equals(line) && line.contains("NativeError") && !line.contains("SQLState = S1000, NativeError = 0")) {
					// 将错误信息记录下来
					list.add(line);
				}
			}
			return list;
		} catch (IOException e) {
			log.error("get process report error", e);
			return list;
		} finally {
			process.destroy();
		}
	}
}
