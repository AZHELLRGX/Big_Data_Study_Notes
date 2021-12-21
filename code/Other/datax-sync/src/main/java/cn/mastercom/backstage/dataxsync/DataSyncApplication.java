package cn.mastercom.backstage.dataxsync;

import cn.mastercom.backstage.dataxsync.config.ChannelConfig;
import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.dao.MainMapper;
import cn.mastercom.backstage.dataxsync.service.DataService;
import cn.mastercom.backstage.dataxsync.task.DistributeTask;
import cn.mastercom.backstage.dataxsync.util.OwnUtils;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class DataSyncApplication implements ApplicationRunner {

	private DataService dataService;

	private MainMapper mainMapper;

	private TaskConfig taskConfig;

	private ChannelConfig channelConfig;

	private static Logger logger = LoggerFactory.getLogger(DataSyncApplication.class);

	@Autowired
	public DataSyncApplication(DataService dataService, MainMapper mainMapper, TaskConfig taskConfig, ChannelConfig channelConfig) {
		this.dataService = dataService;
		this.mainMapper = mainMapper;
		this.taskConfig = taskConfig;
		this.channelConfig = channelConfig;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		init();
		createTimeJob();
	}

	private void init() {
		//初始化服务器信息
		StaticData.init(mainMapper.getServerConfigInfoList());
	}

	private void createTimeJob() {
		//新建定时任务，任务完成10s后，再次执行任务
		ScheduledExecutorService addTaskExecutor = Executors.newSingleThreadScheduledExecutor();
		addTaskExecutor.scheduleWithFixedDelay(this::job, 1, 10, TimeUnit.SECONDS);
	}

	private void job() {
		try {
			tryJob();
		} catch (Exception e) {
			logger.error("数据同步，调度线程报错", e);
		}
	}

	private void tryJob(){
		if (StaticData.getWaitTask().size() < StaticData.DEAL_QUANTITY * 2) {
			dataService.addTasks();
			//无新任务
			if (StaticData.getWaitTask().isEmpty()) {
				OwnUtils.sleep(50, TimeUnit.SECONDS);
				return;
			}
		}
		distributeTasks();
	}

	/**
	 * 分发任务给线程池进行执行
	 */
	private void distributeTasks() {
		new DistributeTask(mainMapper, taskConfig, channelConfig).distribute();
	}

	public static void main(String[] args) {
		SpringApplication.run(DataSyncApplication.class, args);
		logger.info("Program started successfully.");
	}
}
