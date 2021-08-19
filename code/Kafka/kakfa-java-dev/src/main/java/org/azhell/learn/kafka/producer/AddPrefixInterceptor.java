package org.azhell.learn.kafka.producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义拦截器
 */
public class AddPrefixInterceptor implements ProducerInterceptor<String, Company> {
    private final AtomicLong sendSuccess = new AtomicLong(0);
    private final AtomicLong sendFailure = new AtomicLong(0);

    @Override
    public ProducerRecord<String, Company> onSend(ProducerRecord<String, Company> producerRecord) {
        // 消息发送前为 修改原来的对象信息
        producerRecord.value().setAddress("广东省深圳市" + producerRecord.value().getAddress());
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // 完成消息发送成功率的功能
        if (exception == null) {
            sendSuccess.incrementAndGet();
        } else {
            sendFailure.incrementAndGet();
        }
    }

    @Override
    public void close() {
        double successRadio = (double) sendSuccess.get() / (sendSuccess.get() + sendFailure.get());
        System.out.println("消息的发送成功率是:" + successRadio);
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // do nothing
    }
}
