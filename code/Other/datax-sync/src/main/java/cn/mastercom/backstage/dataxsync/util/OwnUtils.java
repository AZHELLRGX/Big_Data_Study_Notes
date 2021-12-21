package cn.mastercom.backstage.dataxsync.util;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author StarNight
 * @date 2020/9/9 9:08
 */

public class OwnUtils {
    private OwnUtils(){}

    /**
     * 判断List是否为空 或 长度为0
     * @param list -
     * @return -
     */
    public static<T> boolean isNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 线程 sleep函数
     * 封装try-catch
     * 添加TimeUnit 使得时间更加直观
     * @param time 时间
     * @param timeUnit 时间单位
     */
    public static void sleep(int time, TimeUnit timeUnit){
        try {
            Thread.sleep(timeUnit.toMillis(time));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
