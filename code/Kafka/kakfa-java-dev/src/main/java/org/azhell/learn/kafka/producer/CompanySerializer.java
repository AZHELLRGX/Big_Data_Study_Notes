package org.azhell.learn.kafka.producer;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 自定义序列化器
 */
public class CompanySerializer implements Serializer<Company> {

    @Override
    public void configure(Map configs, boolean isKey) {
        // do nothing
    }

    @Override
    public byte[] serialize(String topic, Company data) {
        if (data == null) {
            return new byte[0];
        }
        byte[] name;
        byte[] address;
        try {
            if (data.getName() != null) {
                name = ("company name is :" + data.getName()).getBytes(StandardCharsets.UTF_8);
            } else {
                name = new byte[0];
            }
            if (data.getAddress() != null) {
                address = ("\tcompany address is : " + data.getAddress()).getBytes(StandardCharsets.UTF_8);
            } else {
                address = new byte[0];
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(name.length + address.length);
            byteBuffer.put(name);
            byteBuffer.put(address);
            return byteBuffer.array();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Company data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
