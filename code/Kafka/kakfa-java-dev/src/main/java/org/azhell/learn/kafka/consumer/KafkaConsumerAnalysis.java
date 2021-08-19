package org.azhell.learn.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerAnalysis {
    private static final String BROKER_LIST = "192.168.2.101:9092,192.168.2.102:9092,192.168.2.103:9092";
    private static final String TOPIC = "topic-demo";
    private static final String GROUP_ID = "group-demo";
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);

    private static Properties initConfig() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_LIST);
        // 设置消息的序列化组件
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // 设置消费者组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer.client.id.demo");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        return properties;
    }

    public static void main(String[] args) {
        Properties properties = initConfig();
        try (KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties)) {
            kafkaConsumer.subscribe(Collections.singletonList(TOPIC));
            while (RUNNING.get()) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofNanos(1000));
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println("topic is " + consumerRecord.topic() + " partition is " + consumerRecord.partition()
                            + " offset is " + consumerRecord.offset());
                    System.out.println("key is " + consumerRecord.key() + " value is " + consumerRecord.value());
                }
                // 手动同步提交方式
                kafkaConsumer.commitSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
