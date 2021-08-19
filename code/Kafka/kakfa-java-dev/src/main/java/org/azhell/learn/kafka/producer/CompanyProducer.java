package org.azhell.learn.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CompanyProducer {
    private static final String BROKER_LIST = "192.168.2.101:9092,192.168.2.102:9092,192.168.2.103:9092";
    private static final String TOPIC = "topic-demo";

    private static Properties initConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BROKER_LIST);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CompanySerializer.class.getName());
        // 指定分区器
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CompanyPartitioner.class.getName());
        // 指定拦截器
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, AddPrefixInterceptor.class.getName());
        // 多个拦截器使用逗号分割
        //properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, AddPrefixInterceptor.class.getName()+"," +  AddPrefixInterceptor.class.getName());

        properties.put("client.id", "producer.client.id.company");
        return properties;
    }

    public static void main(String[] args) {
        Properties properties = initConfig();
        try (KafkaProducer<String, Company> kafkaProducer = new KafkaProducer<>(properties)) {
            ProducerRecord<String, Company> producerRecord = new ProducerRecord<>(TOPIC, new Company("mastercom", "粤海街道"));
            kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                } else {
                    System.out.println(metadata.partition() + "-" + metadata.offset());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
