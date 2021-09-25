package org.azhell.learn.flink.apitest.source;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.azhell.learn.flink.apitest.beans.SensorReading;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 自定义source
 */
public class CustomizeSourceTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(new CustomizeSourceFunction()).print();

        env.execute();
    }

    private static class CustomizeSourceFunction implements SourceFunction<SensorReading> {

        private boolean flag = true;

        @Override
        public void run(SourceContext<SensorReading> ctx) throws Exception {
            // 定义一个随机数发生器
            Random random = new Random();

            // 设置10个传感器的初始温度
            Map<String, Double> sensorTempMap = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                sensorTempMap.put("sensor_" + (i + 1), 60 + random.nextGaussian() * 20);
            }

            while (flag) {
                for (String sensorId : sensorTempMap.keySet()) {
                    Double newTemp = sensorTempMap.get(sensorId) + random.nextGaussian();
                    sensorTempMap.put(sensorId, newTemp);
                    ctx.collect(new SensorReading(sensorId, System.currentTimeMillis(),
                            newTemp));
                }
                Thread.sleep(1000L);
            }
        }

        @Override
        public void cancel() {
            flag = false;
        }
    }
}
