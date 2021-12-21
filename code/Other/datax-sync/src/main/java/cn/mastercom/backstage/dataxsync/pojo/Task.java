package cn.mastercom.backstage.dataxsync.pojo;

import java.util.Queue;

/**
 * @author StarNight
 * @date 2021/1/5 8:35
 * @description 任务
 */

public class Task {

    /**
     * 完整表名
     * CompleteNameWithIp
     */
    private String table;

    /**
     * 任务的list
     */
    private Queue<DataSyncRecord> recordQueue;

    public Task(String table, Queue<DataSyncRecord> recordQueue) {
        this.table = table;
        this.recordQueue = recordQueue;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Queue<DataSyncRecord> getRecordQueue() {
        return recordQueue;
    }

    public void setRecordQueue(Queue<DataSyncRecord> recordQueue) {
        this.recordQueue = recordQueue;
    }
}
