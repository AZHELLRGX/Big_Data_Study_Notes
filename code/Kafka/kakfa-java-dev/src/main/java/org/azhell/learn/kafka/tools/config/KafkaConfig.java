package org.azhell.learn.kafka.tools.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@EnableKafka
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    @Autowired
    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean("kafkaListenerContainerFactory")
    // configurer can autowire the listener configs
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    public ConsumerFactory<Object, Object> consumerFactory() {
        KafkaProperties.Consumer propertiesConsumer = kafkaProperties.getConsumer();
        return new DefaultKafkaConsumerFactory<>(propertiesConsumer.buildProperties());
    }
}
