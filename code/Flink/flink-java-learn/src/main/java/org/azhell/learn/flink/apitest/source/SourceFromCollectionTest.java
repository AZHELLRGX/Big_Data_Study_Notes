package org.azhell.learn.flink.apitest.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.azhell.learn.flink.apitest.beans.SensorReading;

import java.util.Arrays;

/**
 * 从集合读取数据，并不常用
 */
public class SourceFromCollectionTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 从集合创建流
        DataStreamSource<SensorReading> sensorReadingDs = env.fromCollection(Arrays.asList(
                new SensorReading("sensor_1", 1547718199L, 35.8),
                new SensorReading("sensor_6", 1547718201L, 15.4),
                new SensorReading("sensor_7", 1547718202L, 6.7),
                new SensorReading("sensor_10", 1547718205L, 38.1)));
        // 随机元素创建流
        DataStreamSource<Integer> intDs = env.fromElements(1, 3, 54, 6);

        // 打印输出
        sensorReadingDs.print("sensorReadings");
        intDs.print("intData");

        env.execute();
    }
}
