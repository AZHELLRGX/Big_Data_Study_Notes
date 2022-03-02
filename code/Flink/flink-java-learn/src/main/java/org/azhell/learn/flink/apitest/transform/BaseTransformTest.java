package org.azhell.learn.flink.apitest.transform;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 基本转换算子测试
 */
public class BaseTransformTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从文件读取数据
        DataStream<String> inputStream = env.readTextFile("src/main/resources/data/sensor.txt");

        // 1. map，把String转换成长度输出
        DataStream<Integer> mapStream = inputStream.map(String::length);

        // 2. flatmap，按逗号分字段
        DataStream<String> flatMapStream = inputStream.flatMap((FlatMapFunction<String, String>) (value, out) -> {
            String[] fields = value.split(",");
            for (String field : fields)
                out.collect(field);
        }).returns(String.class); // flatMap转换称为lambda表达式的时候，需要显式设置返回类型

        // 3. filter, 筛选sensor_1开头的id对应的数据
        DataStream<String> filterStream = inputStream.filter(value -> value.startsWith("sensor_1"));
        // 过滤数据的时候，只有有数据满足条件时才会输出

        // 打印输出
        mapStream.print("map");
        flatMapStream.print("flatMap");
        filterStream.print("filter");

        env.execute();
    }

}
