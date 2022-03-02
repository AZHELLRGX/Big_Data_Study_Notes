package org.azhell.learn.flink.apitest.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.azhell.learn.flink.apitest.beans.SensorReading;

/**
 * 从文件读取数据
 */
public class SourceFromFileTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> sensorDs = env.readTextFile("src/main/resources/data/sensor.txt");
        SingleOutputStreamOperator<SensorReading> operator = sensorDs.map(s -> {
            String[] strings = s.split(",");
            return new SensorReading(strings[0], Long.parseLong(strings[1]), Double.parseDouble(strings[2]));
        });

        operator.print();

        env.execute();
    }
}
