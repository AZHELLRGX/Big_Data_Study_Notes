package cn.mastercom.backstage.dataxsync.util;

import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import cn.mastercom.backstage.dataxsync.pojo.Task;
import cn.mastercom.backstage.dataxsync.task.OwnThreadPool;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放静态数据 1、数据库连接池
 */

public class StaticData {

	private static Logger logger = LoggerFactory.getLogger(StaticData.class);

	private StaticData() {
	}

	/**
	 * cpu核数，由于Runtime.getRuntime().availableProcessors()可能报错
	 * 暂时默认为8
	 */
	public static final int CPU_SIZE = 8;

	/**
	 * 一次分发任务处理的任务数量
	 */
	public static final int DEAL_QUANTITY = CPU_SIZE * 2;

	/**
	 * 任务处理线程池
	 */
	private static OwnThreadPool threadPool = new OwnThreadPool(CPU_SIZE);

	/**
	 * 在进行/在队列中的任务 集合
	 * key--全量表名--服务器+库+表
	 */
	private static Map<String, Queue<DataSyncRecord>> taskMap = new ConcurrentHashMap<>(CPU_SIZE * 10);

	/**
	 * 等待分发的任务
	 */
	private static Queue<Task> waitTask = new LinkedList<>();

	/**
	 * key--服务器id
	 */
	private static Map<Integer, ServerConfigInfo> serverConfigMap = new HashMap<>();
	private static Map<Integer, DruidDataSource> dBPoolMap = new HashMap<>();

	/**
	 * key--Guid
	 */
	private static Map<String, Communication> taskCommunicationMap = new ConcurrentHashMap<>();

	public static OwnThreadPool getThreadPool() {
		return threadPool;
	}

	public static Map<String, Queue<DataSyncRecord>> getTaskMap() {
		return taskMap;
	}

	public static Queue<Task> getWaitTask() {
		return waitTask;
	}

	// 注册
	public static void registerTaskCommunication(String guid) {
		taskCommunicationMap.put(guid, new Communication());
	}

	// 获取
	public static Communication getTaskCommunication(String guid) {
		return taskCommunicationMap.remove(guid);
	}

	public static void init(List<ServerConfigInfo> serverConfigInfos) {
		for (ServerConfigInfo serverConfigInfo : serverConfigInfos) {
			serverConfigMap.put(serverConfigInfo.getId(), serverConfigInfo);
		}
	}

	/**
	 * 获取服务器信息
	 */
	public static ServerConfigInfo getServerConfigInfo(int serverId) {
		return serverConfigMap.get(serverId);
	}

	/**
	 * 塞入服务器信息
	 */
	public static void addServerConfigInfo(ServerConfigInfo serverConfigInfo) {
		serverConfigMap.put(serverConfigInfo.getId(), serverConfigInfo);
	}

	/**
	 * 获取对应服务器连接池
	 *
	 * @return
	 */
	public static synchronized DruidPooledConnection getPooledConnectionByServerId(int serverId) {
		if (!dBPoolMap.containsKey(serverId)) {
			ServerConfigInfo serverConfigInfo = getServerConfigInfo(serverId);
			DruidDataSource ds = new DruidDataSource();
			String url;
			String driverClass;
			String testSql = "select 1";
			switch (serverConfigInfo.getType().toLowerCase()) {
			case "mysql":
				driverClass = DataBaseType.MySql.getDriverClassName();
				// jdbc:mysql://192.168.2.101:3306/mysql?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useCursorFetch=true
				url = "jdbc:mysql://" + serverConfigInfo.getUrl()
						+ "/mysql?useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&rewriteBatchedStatements=true&useCursorFetch=true";
				break;
			case "oracle":
				driverClass = DataBaseType.Oracle.getDriverClassName();
				url = "jdbc:oracle:thin:@" + serverConfigInfo.getUrl();
				testSql = "select 1 from dual";
				break;
			case "sybaseiq":
				driverClass = DataBaseType.SybaseIQ.getDriverClassName();
				// jdbc:sybase:Tds:<host>:<port>?ServiceName=<database_name>
				// jdbc:jtds:sybase://192.168.102.100:5000/test
				url = "jdbc:sybase:Tds:" + serverConfigInfo.getUrl();
				break;
			case "sqlserver":
			default:
				try {
					driverClass = DataBaseType.SQLServer.getDriverClassName();
				} catch (Exception e) {
					logger.error("错误", e);
					driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
				}
				// 为了使用复制表的存储过程，这里数据库还是写成MTNOH_AAA_DB
				// 数据库配置如果有端口，因为配置中是逗号（bcp只能使用逗号），程序则无法连接，所以需要将逗号替换为冒号
				String url0 = serverConfigInfo.getUrl();
				if (url0.contains(",")) {
					url0 = url0.replace(",", ":");
				}
				url = "jdbc:sqlserver://" + url0;
				break;
			}

			ds.setDriverClassName(driverClass);
			ds.setUrl(url);
			// 设置用户名
			ds.setUsername(serverConfigInfo.getUsername());
			// 设置密码
			ds.setPassword(serverConfigInfo.getPassword());

			//最大连接池数量
			ds.setMaxActive(30);
			//最小连接池数量
			ds.setMinIdle(10);
			//有两个含义：
			//1) Destroy线程会检测连接的间隔时间, 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
			//2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
			ds.setTimeBetweenEvictionRunsMillis(60000);
			//连接保持空闲而不被驱逐的最长时间
			ds.setMinEvictableIdleTimeMillis(300000);
			//建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
			ds.setTestWhileIdle(true);
			// question: 同上 不知道为什么设置两次?
			ds.setTimeBetweenEvictionRunsMillis(2000);
			//申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
			ds.setTestOnBorrow(false);
			//归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
			//这里建议配置为TRUE，防止取到的连接不可用
			ds.setTestOnReturn(false);
			//是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭
			ds.setPoolPreparedStatements(false);
			//要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
			// 强制归还时间是24小时
			// 通过datasource.getConnontion() 取得的连接必须在removeAbandonedTimeout这么多秒内调用close(),要不我就弄死你.(就是conn不能超过指定的租期)
			// question:removeAbandoned 这个没设置成true 时间不知道起不起作用
			ds.setRemoveAbandonedTimeout(24 * 3600);
			//用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
			ds.setValidationQuery(testSql);
			dBPoolMap.put(serverId, ds);
		}
		try {
			return dBPoolMap.get(serverId).getConnection(5000);
		} catch (Exception e) {
			logger.error("获取数据库连接出现异常", e);
			return null;
		}
	}
}
