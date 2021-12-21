package cn.mastercom.backstage.dataxsync.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author StarNight
 * @date 2020/9/9 9:52
 */

public class OwnThreadPool extends ThreadPoolExecutor {

    public OwnThreadPool(int cpuSize) {
        super(cpuSize + 1, cpuSize * 2, 40, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(cpuSize * 10));
    }

}
