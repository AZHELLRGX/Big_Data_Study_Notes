package org.azhell.learn.flink.apitest.transform;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.azhell.learn.flink.apitest.beans.SensorReading;

/**
 * 聚合操作测试
 * KeyedStream  → DataStream
 */
public class ReduceTransformTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> inputStream = env.readTextFile("src/main/resources/data/sensor.txt");
        //  转换成 SensorReading 类型
        DataStream<SensorReading> dataStream = inputStream.map(
                value -> {
                    String[] fields = value.split(",");
                    return new SensorReading(fields[0], new Long(fields[1]), new
                            Double(fields[2]));
                });
        //  分组
        KeyedStream<SensorReading, String> keyedStream = dataStream.keyBy(SensorReading::getId);
        // reduce 聚合，取最小的温度值，并输出当前的时间戳
        DataStream<SensorReading> reduceStream = keyedStream.reduce(
                (value1, value2) -> new SensorReading(
                        value1.getId(),
                        value2.getTimestamp(),
                        Math.min(value1.getTemperature(), value2.getTemperature())));
        reduceStream.print();

        env.execute();
    }
}
