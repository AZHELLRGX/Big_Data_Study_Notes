package org.azhell.learn.flink.sink;

import org.apache.flink.kafka.shaded.org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.azhell.learn.flink.tool.PropertiesUtil;
import org.azhell.learn.flink.tool.Utils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 通用的kafka写出
 */
public class FlinkKafkaSinkSupport {
    private FlinkKafkaSinkSupport(){}

    public static FlinkKafkaProducer<String> getProducerSink() {
        final Properties properties = PropertiesUtil.getBundleWithParameterOverride(PkgConst.configFileName);
        String topic = properties.getProperty(PkgConst.topicConfigKey);
        properties.remove(PkgConst.topicConfigKey);
        Utils.print("当前生产 Kafka topic：{}", topic);
        return new FlinkKafkaProducer<>(
                topic,
                new ProducerStringSerializationSchema(topic),
                properties,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE);
    }

    static class ProducerStringSerializationSchema implements KafkaSerializationSchema<String> {

        private static final long serialVersionUID = 2805409336149499687L;
        private final String topic;

        public ProducerStringSerializationSchema(String topic) {
            super();
            this.topic = topic;
        }

        @Override
        public ProducerRecord<byte[], byte[]> serialize(String element, Long timestamp) {
            // 将字符串转换为字节数组
            return new ProducerRecord<>(topic, element.getBytes(StandardCharsets.UTF_8));
        }

    }
}
