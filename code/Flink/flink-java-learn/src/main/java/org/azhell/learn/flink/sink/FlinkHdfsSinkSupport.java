package org.azhell.learn.flink.sink;


import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Flink利用filesystem的connector写出数据的通用模版
 */
public class FlinkHdfsSinkSupport {

    private FlinkHdfsSinkSupport() {
        // private hdfs
    }

    // 写出数据到HDFS的sink
    public static StreamingFileSink<Tuple2<String, String>> getStreamFileSink(String path) {
        final DefaultRollingPolicy<Tuple2<String, String>, String> build = DefaultRollingPolicy.builder()
                .withInactivityInterval(Duration.ofMinutes(5).toMillis())  //5m空闲，就滚动写入新的文件
                .withRolloverInterval(Duration.ofMinutes(10).toMillis()) //不论是否空闲，超过10分钟就写入新文件，默认60s。这里设置为10m
                .withMaxPartSize(134217728) // 设置每个文件的最大大小 ,默认是128M
                .build();

        return StreamingFileSink
                // 输出文件格式为txt文本格式
                .forRowFormat(new Path(path), new CustomizedStringEncoder(StandardCharsets.UTF_8.toString()))//设置文件路径，以及文件中的编码格式
                .withBucketAssigner(new CustomizedBucketAssigner())//设置自定义分桶
                .withRollingPolicy(build)//设置文件滚动条件
                .withBucketCheckInterval(TimeUnit.MINUTES.toMillis(5))//设置检查点
                .build();
    }

    static class CustomizedStringEncoder extends SimpleStringEncoder<Tuple2<String, String>> {


        private static final long serialVersionUID = 8273393755470504433L;
        private final String charsetName;

        private transient Charset charset;

        public CustomizedStringEncoder(String charsetName) {
            this.charsetName = charsetName;
        }

        @Override
        public void encode(Tuple2<String, String> element, OutputStream stream) throws IOException {
            if (charset == null) {
                charset = Charset.forName(charsetName);
            }
            stream.write(element.f1.getBytes(charset));
        }
    }

    static class CustomizedBucketAssigner implements BucketAssigner<Tuple2<String, String>, String> {
        private static final long serialVersionUID = -7229684350852113483L;

        @Override
        public String getBucketId(Tuple2<String, String> value, BucketAssigner.Context context) {
            // 数据路径，目录
            return value.f0;
        }

        @Override
        public SimpleVersionedSerializer<String> getSerializer() {
            return SimpleVersionedStringSerializer.INSTANCE;
        }
    }

}
