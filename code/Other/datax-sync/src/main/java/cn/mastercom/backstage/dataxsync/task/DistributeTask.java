package cn.mastercom.backstage.dataxsync.task;

import cn.mastercom.backstage.dataxsync.config.ChannelConfig;
import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.dao.MainMapper;
import cn.mastercom.backstage.dataxsync.pojo.Task;
import cn.mastercom.backstage.dataxsync.util.OwnUtils;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class DistributeTask {

    private static final Logger LOG = LoggerFactory.getLogger(DistributeTask.class);

    private MainMapper mainMapper;
    private TaskConfig taskConfig;
    private ChannelConfig channelConfig;

    public DistributeTask(MainMapper mainMapper, TaskConfig taskConfig, ChannelConfig channelConfig) {
        this.mainMapper = mainMapper;
        this.taskConfig = taskConfig;
        this.channelConfig = channelConfig;
    }

    public void distribute() {
        try {
            //还有待分配的任务
            if (!StaticData.getWaitTask().isEmpty()) {
                LOG.info("---开始分配任务----;当前任务状态---：队列中：{},等待中：{}",
                        StaticData.getThreadPool().getQueue().size(),StaticData.getWaitTask().size());
                distributeTasks();
                LOG.info("....分配任务结束....");
            }
        } catch (Exception e) {
            LOG.error("分配任务出现异常", e);
        }
    }

    /**
     * 向线程池中加入dealQuantity数量的任务
     * 触发拒绝策略则让分发线程延时10s
     * 使得下次分发任务延迟10s执行，给线程池缓存时间。
     */
    private void distributeTasks() {
        int dealQuantity = Math.min(StaticData.getWaitTask().size(), StaticData.DEAL_QUANTITY);

        for (int i = 0; i < dealQuantity; i++) {
            Task task = StaticData.getWaitTask().peek();
            try {
                StaticData.getThreadPool().execute(new TaskExecutor(mainMapper, taskConfig, channelConfig,task));
                //任务成功加入线程池，才移除任务.
                StaticData.getTaskMap().put(task.getTable(),StaticData.getWaitTask().poll().getRecordQueue());
            } catch (RejectedExecutionException rejectedExecution) {
                LOG.warn("=============线程池队列已满=============");
                OwnUtils.sleep(20, TimeUnit.SECONDS);
                return;
            }
        }
    }

}
