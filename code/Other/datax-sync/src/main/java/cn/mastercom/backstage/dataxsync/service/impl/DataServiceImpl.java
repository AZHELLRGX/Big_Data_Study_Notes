package cn.mastercom.backstage.dataxsync.service.impl;

import cn.mastercom.backstage.dataxsync.config.DbClientConfig;
import cn.mastercom.backstage.dataxsync.config.TaskConfig;
import cn.mastercom.backstage.dataxsync.dao.MainMapper;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import cn.mastercom.backstage.dataxsync.pojo.Task;
import cn.mastercom.backstage.dataxsync.service.DataService;
import cn.mastercom.backstage.dataxsync.util.OwnUtils;
import cn.mastercom.backstage.dataxsync.util.StaticData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

    private DbClientConfig dbClientConfig;

    private MainMapper mainMapper;

    private TaskConfig taskConfig;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");

    @Autowired
    public DataServiceImpl(DbClientConfig dbClientConfig, MainMapper mainMapper, TaskConfig taskConfig) {
        this.dbClientConfig = dbClientConfig;
        this.mainMapper = mainMapper;
        this.taskConfig = taskConfig;
    }

    /**
     * 向任务池补充任务
     */
    @Override
    public void addTasks() {
        if (StaticData.getWaitTask().size() >= 1000) {
            LOG.warn("当前任务堆积种类已经超过1000，请检查程序是否存在异常或重启程序...");
        }

        List<DataSyncRecord> dataSyncRecordList = mainMapper.getDataSyncRecord(taskConfig.getIds());
        LOG.info("开始补充任务....;本次获取到的新记录{}条。", dataSyncRecordList.size());
        if (dataSyncRecordList.isEmpty()) {
            return;
        }
        //任务涉及日期 的集合
        Set<String> dateStrs = new HashSet<>();
        List<DataSyncRecord> validDataSyncRecordList = new ArrayList<>();
        List<DataSyncRecord> invalidDataSyncRecordList = new ArrayList<>();

        // 任务的有效性校验步骤放在插入任务的时候来做
        for (DataSyncRecord dataSyncRecord : dataSyncRecordList) {
            String format = simpleDateFormat.format(dataSyncRecord.getDataTime());
            dataSyncRecord.setDateFormat(format);
            dateStrs.add(format);
            // 检测任务是否可执行
            if (validateDataSyncRecord(dataSyncRecord)) {
                // 有效，插入任务
                // 防止重复读取任务，读取任务的时候就变更其状态为2
                // 标记任务为正在执行
                mainMapper.updateDataSyncRecord(2, dataSyncRecord.getGuid());
                validDataSyncRecordList.add(dataSyncRecord);
            } else {
                invalidDataSyncRecordList.add(dataSyncRecord);
            }
        }

        createDayTable(dateStrs);
        handleInvalidRecord(invalidDataSyncRecordList);
        handleValidRecord(validDataSyncRecordList);
    }

    /**
     * 处理有效的记录
     * 往StaticData中插入任务
     * 初始化线程池
     *
     * @param validDataSyncRecordList -
     */
    private void handleValidRecord(List<DataSyncRecord> validDataSyncRecordList) {
        if (OwnUtils.isNullOrEmpty(validDataSyncRecordList)) {
            return;
        }
        //按表名进行分组
        Map<String, List<DataSyncRecord>> groupMap = new HashMap<>();
        for (DataSyncRecord dataSyncRecord : validDataSyncRecordList) {
            String table = dataSyncRecord.getTableInfoTarget().getCompleteNameWithIp();
            if (groupMap.containsKey(table)) {
                groupMap.get(table).add(dataSyncRecord);
            } else {
                List<DataSyncRecord> list = new ArrayList<>();
                list.add(dataSyncRecord);
                groupMap.put(table, list);
            }
        }

        int i = 0;
        //
        for (Map.Entry<String, List<DataSyncRecord>> entry : groupMap.entrySet()) {
            if (StaticData.getTaskMap().containsKey(entry.getKey())) {
                synchronized (StaticData.getTaskMap()) {
                    if (StaticData.getTaskMap().containsKey(entry.getKey())) {
                        i++;
                        StaticData.getTaskMap().get(entry.getKey()).addAll(entry.getValue());
                        LOG.info("{} 追加一条任务，相关任务数量：{}", entry.getKey(),
                                StaticData.getTaskMap().get(entry.getKey()).size());
                    }
                }
            } else {
                Task task = new Task(entry.getKey(), new ConcurrentLinkedQueue<>(entry.getValue()));
                StaticData.getWaitTask().offer(task);
            }
        }

        LOG.info("有效记录{}条,任务分配---进行中：{},等待中：{}",
                validDataSyncRecordList.size(), i, groupMap.size() - i);
    }

    /**
     * 处理无效的记录
     * 无效，更新任务状态
     * 插入日志
     *
     * @param invalidDataSyncRecordList -
     */
    private void handleInvalidRecord(List<DataSyncRecord> invalidDataSyncRecordList) {
        if (OwnUtils.isNullOrEmpty(invalidDataSyncRecordList)) {
            return;
        }
        for (DataSyncRecord dataSyncRecord : invalidDataSyncRecordList) {
            // 校验失败后需要将时间插进去
            dataSyncRecord.getDataSyncLog().setBeginTime(new Date());
            dataSyncRecord.getDataSyncLog().setEndTime(new Date());
            dataSyncRecord.getDataSyncLog().setSuccess(false);
            dataSyncRecord.setStatusId(7);
            mainMapper.updateDataSyncRecord(dataSyncRecord.getStatusId(), dataSyncRecord.getGuid());

            mainMapper.insertDataSyncLog(dataSyncRecord.getDataSyncLog(), dataSyncRecord.getDateFormat());
        }
        LOG.info("无效记录{}条，具体原因查看表TB_数据同步_日志_记录_DD_数据时间", invalidDataSyncRecordList.size());
    }

    private boolean validateDataSyncRecord(DataSyncRecord dataSyncRecord) {
        dataSyncRecord.getDataSyncLog().setGuid(dataSyncRecord.getGuid());

        // 数据库连接信息是否存在
        // 查的本地数据库
        if (!existsServerConfigInfo(dataSyncRecord.getSourceServerId())) {
            dataSyncRecord.getDataSyncLog().setInfo(String.format("没有找到%d的对应服务器连接信息", dataSyncRecord.getSourceServerId()));
            return false;
        }
        if (!existsServerConfigInfo(dataSyncRecord.getTargetServerId())) {
            dataSyncRecord.getDataSyncLog().setInfo(String.format("没有找到%d的对应服务器连接信息", dataSyncRecord.getTargetServerId()));
            return false;
        }

        try {
            dataSyncRecord.init(StaticData.getServerConfigInfo(dataSyncRecord.getSourceServerId()),
                    StaticData.getServerConfigInfo(dataSyncRecord.getTargetServerId()));
        } catch (Exception e) {
            LOG.error("dataSyncRecord.init error", e);
            dataSyncRecord.getDataSyncLog().setInfo("dataSyncRecord.init error:" + e.getMessage());
            return false;
        }

        // 如果select 语句有且不为select *;则应该是部分字段同步，不需要走校验逻辑
        if (StringUtils.isNotBlank(dataSyncRecord.getSqlStr())
                && !StringUtils.contains(StringUtils.deleteWhitespace(dataSyncRecord.getSqlStr().toUpperCase()), "SELECT*")) {
            return true;
        }

        // 源表与目标表的字段数量是否一致(如果是模版表则对比的是模版表的值)
        if (dataSyncRecord.getTableInfoSource().getColumnSize() != dataSyncRecord.getTableInfoTarget().getColumnSize()) {
            dataSyncRecord.getDataSyncLog().setInfo("源表与目标表的数据字段数量不一致");
            return false;
        } else if (StaticData.getServerConfigInfo(dataSyncRecord.getSourceServerId()).getType()
                .equalsIgnoreCase(StaticData.getServerConfigInfo(dataSyncRecord.getTargetServerId()).getType())) {
            // 如果数据库类型一致，则继续对比两个表的数据类型
            List<Integer> columnTypesSource = dataSyncRecord.getTableInfoSource().getColumnTypes();
            List<Integer> columnTypesTarget = dataSyncRecord.getTableInfoTarget().getColumnTypes();
            for (int i = 0; i < columnTypesSource.size(); i++) {
                if (!columnTypesSource.get(i).equals(columnTypesTarget.get(i))) {
                    dataSyncRecord.getDataSyncLog().setInfo("源表与目标表的第" + (i + 1) + "个数据字段类型不一致");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean existsServerConfigInfo(int id) {
        // 服务器连接信息
        if (StaticData.getServerConfigInfo(id) == null) {
            ServerConfigInfo serverConfigInfo = mainMapper.getServerConfigInfoById(id);
            if (serverConfigInfo != null) {
                StaticData.addServerConfigInfo(serverConfigInfo);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 创建日志表
     *
     * @param dateStrs 日期
     */
    private void createDayTable(Set<String> dateStrs) {
        String[] tableNames = new String[]{"TB_数据同步_日志_记录"};
        if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(dbClientConfig.getDriverClassName())) {
            for (String dateStr : dateStrs) {
                for (String tableName : tableNames) {
                    mainMapper.createSqlServerDayTable(tableName, dateStr);
                }
            }
        } else if ("com.mysql.jdbc.Driver".equals(dbClientConfig.getDriverClassName())) {
            for (String dateStr : dateStrs) {
                for (String tableName : tableNames) {
                    mainMapper.createMysqlDayTable(tableName, dateStr);
                }
            }
        }
    }
}
