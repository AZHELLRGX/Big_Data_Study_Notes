package org.azhell.learn.flink.apitest.transform;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.azhell.learn.flink.apitest.beans.SensorReading;

/**
 * 滚动聚合转换
 * DataStream  → KeyedStream
 */
public class RollingAggregationTransformTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        // 从文件读取数据
        DataStream<String> inputStream = env.readTextFile("src/main/resources/sensor.txt");

        // 转换成SensorReading类型
        DataStream<SensorReading> dataStream = inputStream.map(line -> {
            String[] fields = line.split(",");
            return new SensorReading(fields[0], new Long(fields[1]), new Double(fields[2]));
        });

        // 分组
        KeyedStream<SensorReading, Tuple> keyedStream = dataStream.keyBy("id"); // pojo的字段名称
        KeyedStream<SensorReading, String> keyedStream1 = dataStream.keyBy(SensorReading::getId); // 字段值进行分组

        DataStream<Long> dataStream1 = env.fromElements(1L, 34L, 4L, 657L, 23L);

        // 自定义KeySelector来实现分组逻辑
        KeyedStream<Long, Integer> keyedStream2 = dataStream1.keyBy(new KeySelector<Long, Integer>() {
            @Override
            public Integer getKey(Long value) throws Exception {
                return value.intValue() % 2;
            }
        });

        // 滚动聚合，取当前最大的温度值
        DataStream<SensorReading> resultStream = keyedStream.maxBy("temperature");
        // 需要注意max和maxBy的区别

        resultStream.print("result");

        keyedStream1.print("key1");
        keyedStream2.sum(0).print("key2");
        env.execute();
    }
}
