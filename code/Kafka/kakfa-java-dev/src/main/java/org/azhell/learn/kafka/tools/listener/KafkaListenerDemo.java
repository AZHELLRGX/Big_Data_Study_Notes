package org.azhell.learn.kafka.tools.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaListenerDemo {

    @KafkaListener(topics = "test.mm2.topic", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<String> records, Acknowledgment ack,
                       @Header(KafkaHeaders.OFFSET) String offset) {
        log.info("get a batch of records, the size is {}, current offset is {}", records.size(), offset);
        for (String record : records) {
            log.info("handle one record, its content is '{}'", record);
        }
        log.info("will commit the consumer offset manually");
        ack.acknowledge();
    }
}
