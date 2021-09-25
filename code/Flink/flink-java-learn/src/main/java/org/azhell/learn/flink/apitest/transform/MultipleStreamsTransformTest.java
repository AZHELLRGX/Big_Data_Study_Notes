package org.azhell.learn.flink.apitest.transform;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.azhell.learn.flink.apitest.beans.SensorReading;

import java.util.Collections;

/**
 * 多流转换测试
 * 1、split：DataStream  → SplitStream  select：SplitStream →DataStream
 * 2、Connect：DataStream,DataStream  → ConnectedStreams CoMap,CoFlatMap：ConnectedStreams → DataStream
 * 3、union：DataStream  → DataStream
 */
public class MultipleStreamsTransformTest {

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

        // OutputSelector自定义
        // 该方法已经过时，官方建议：Please use side output instead 但是侧路输出是最底层的API，后续再测试
        SplitStream<SensorReading> splitStream = dataStream.split(new OutputSelector<SensorReading>() {
            @Override
            public Iterable<String> select(SensorReading value) {
                return (value.getTemperature() > 30) ? Collections.singletonList("high") :
                        Collections.singletonList("low");
            }
        });
        // split并未真正的分流，而只是给数据打上了标签，必须使用select方法
        DataStream<SensorReading> highTempStream = splitStream.select("high");
        DataStream<SensorReading> lowTempStream = splitStream.select("low");
        DataStream<SensorReading> allTempStream = splitStream.select("high", "low");

        highTempStream.print();
        lowTempStream.print();
        allTempStream.print();

        //  合流 connect
        DataStream<Tuple2<String, Double>> warningStream = highTempStream.map(new MapFunction<SensorReading, Tuple2<String, Double>>() {
            @Override
            public Tuple2<String, Double> map(SensorReading value) throws Exception {
                return new Tuple2<>(value.getId(), value.getTemperature());
            }
        });
        // 将两个数据类型不一致的流合并在一起
        ConnectedStreams<Tuple2<String, Double>, SensorReading> connectedStreams =
                warningStream.connect(lowTempStream);
        // 两个流分别进行自己的处理，如果流的返回类型不一致，直接使用object作为返回值类型即可
        DataStream<Tuple2<String, String>> resultStream = connectedStreams.map(new CoMapFunction<Tuple2<String, Double>, SensorReading, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> map1(Tuple2<String, Double> value) throws Exception {
                return new Tuple2<>(value.f0, "warning");
            }

            @Override
            public Tuple2<String, String> map2(SensorReading value) throws Exception {
                return new Tuple2<>(value.getId(), "healthy");
            }
        });

        resultStream.print();

        // Union 之前两个流的类型必须是一样的，可以合并多个流
        // union的参数时可变参数列表
        DataStream<SensorReading> unionStream = highTempStream.union(lowTempStream);

        unionStream.print();

        env.execute();
    }
}
