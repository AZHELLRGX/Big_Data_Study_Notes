package org.azhell.learn.flink.sink.test.hdfs;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.azhell.learn.flink.apitest.beans.SensorReading;
import org.azhell.learn.flink.sink.FlinkHdfsParquetSinkSupport;
import org.azhell.learn.flink.sink.FlinkKafkaSinkSupport;
import org.azhell.learn.flink.source.FlinkKafkaSourceSupport;

import java.time.Duration;
import java.util.Random;

public class TestHdfsSink {
    public static void main(String[] args) throws Exception {
        final ParameterTool pt = ParameterTool.fromArgs(args);
        final Configuration conf = new Configuration(pt.getConfiguration());
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.enableCheckpointing(Duration.ofMinutes(3).toMillis());
        final DataStream<String> stringDS = FlinkKafkaSourceSupport.createStringDS(env);

        final SingleOutputStreamOperator<SensorReading> sensorReadingDs = stringDS.map(value -> JSON.parseObject(value, SensorReading.class));
        // 写回去，永动机
        sensorReadingDs.map(sensorReading -> {
            Random random = new Random();
            return JSON.toJSONString(new SensorReading(sensorReading.getId() + "_" + random.nextInt(10), System.currentTimeMillis(), random.nextDouble()));
        }).addSink(FlinkKafkaSinkSupport.getProducerSink());
        sensorReadingDs.addSink(FlinkHdfsParquetSinkSupport.getStreamFileSink("hdfs://master:9000/usr/rgx/test_sink/parquet_sink"));

        env.execute();
    }
}
