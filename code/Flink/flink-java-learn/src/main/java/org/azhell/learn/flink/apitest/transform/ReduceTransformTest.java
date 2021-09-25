package org.azhell.learn.flink.apitest.transform;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
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
        DataStream<String> inputStream = env.readTextFile("src/main/resources/sensor.txt");
        //  转换成 SensorReading 类型
        DataStream<SensorReading> dataStream = inputStream.map(new MapFunction<String, SensorReading>() {
            public SensorReading map(String value) throws Exception {
                String[] fields = value.split(",");
                return new SensorReading(fields[0], new Long(fields[1]), new
                        Double(fields[2]));
            }
        });
        //  分组
        KeyedStream<SensorReading, Tuple> keyedStream = dataStream.keyBy("id");
        // reduce 聚合，取最小的温度值，并输出当前的时间戳
        DataStream<SensorReading> reduceStream = keyedStream.reduce(new ReduceFunction<SensorReading>() {
            @Override
            public SensorReading reduce(SensorReading value1, SensorReading value2)
                    throws Exception {
                return new SensorReading(
                        value1.getId(),
                        value2.getTimestamp(),
                        Math.min(value1.getTemperature(), value2.getTemperature()));
            }
        });

        reduceStream.print();

        env.execute();
    }
}
