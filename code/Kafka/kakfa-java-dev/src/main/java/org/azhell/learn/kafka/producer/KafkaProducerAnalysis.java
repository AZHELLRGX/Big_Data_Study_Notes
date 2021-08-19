package org.azhell.learn.kafka.producer;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class KafkaProducerAnalysis {
    private static final String BROKER_LIST = "192.168.2.101:9092,192.168.2.102:9092,192.168.2.103:9092";
    private static final String TOPIC = "topic-demo";

    private static Properties initConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers",BROKER_LIST);
        // 设置消息的序列化组件
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.RETRIES_CONFIG, 10);
        properties.put("client.id", "producer.client.id.demo");
        return properties;
    }

    public static void main(String[] args) {
        Properties properties = initConfig();
        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties)) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, "hello, scala");
            kafkaProducer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null){
                        exception.printStackTrace();
                    }else{
                        System.out.println(metadata.partition() + "-" + metadata.offset());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
