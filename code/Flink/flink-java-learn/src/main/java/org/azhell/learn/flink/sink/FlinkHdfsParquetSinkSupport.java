package org.azhell.learn.flink.sink;


import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.formats.parquet.avro.ParquetAvroWriters;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import org.azhell.learn.flink.apitest.beans.SensorReading;

import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * Flink利用filesystem的connector写出数据的通用模版
 */
public class FlinkHdfsParquetSinkSupport {

    private FlinkHdfsParquetSinkSupport() {
        // private hdfs
    }

    // 写出数据到HDFS的sink
    public static StreamingFileSink<SensorReading> getStreamFileSink(String path) {
        return StreamingFileSink
                // 输出文件格式为txt文本格式
                //.forRowFormat(new Path(path), new CustomizedStringEncoder(StandardCharsets.UTF_8.toString()))//设置文件路径，以及文件中的编码格式
                .forBulkFormat(new Path(path), ParquetAvroWriters.forReflectRecord(SensorReading.class))
                .withBucketAssigner(new SensorReadingBucketAssigner())//设置自定义分桶
                .withRollingPolicy(OnCheckpointRollingPolicy.build())//设置文件滚动条件
                .withBucketCheckInterval(Duration.ofMinutes(3).toMillis())//设置检查点
                .build();
    }

    static class SensorReadingBucketAssigner implements BucketAssigner<SensorReading, String> {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        @Override
        public String getBucketId(SensorReading sensorReading, BucketAssigner.Context context) {
            return sdf.format(sensorReading.getTimestamp());
        }

        @Override
        public SimpleVersionedSerializer<String> getSerializer() {
            return SimpleVersionedStringSerializer.INSTANCE;
        }
    }
}
