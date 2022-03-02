package org.azhell.learn.flink.source;


import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.azhell.learn.flink.tool.PropertiesUtil;
import org.azhell.learn.flink.tool.Utils;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * 通用的kafka源数据读取
 */
public class FlinkKafkaSourceSupport {
    private FlinkKafkaSourceSupport() {
    }

    public static DataStream<String> createStringDS(
            @Nonnull StreamExecutionEnvironment env) {
        final Properties properties = PropertiesUtil.getBundleWithParameterOverride(PkgConst.configFileName);
        String topic = properties.getProperty(PkgConst.topicConfigKey);
        properties.remove(PkgConst.topicConfigKey);
        Utils.print("当前消费 Kafka topic：{}", topic);
        return env.addSource(new FlinkKafkaConsumer<>(topic, new SimpleStringSchema(), properties));
    }
}
