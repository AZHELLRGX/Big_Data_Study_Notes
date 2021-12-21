package cn.mastercom.backstage.dataxsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "channel")
public class ChannelConfig {

	private int capacity;
	private int byteSpeed;
	private int recordSpeed;
	private int flowControlInterval;
	private int byteCapacity;
	private int bufferSize;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getByteSpeed() {
		return byteSpeed;
	}

	public void setByteSpeed(int byteSpeed) {
		this.byteSpeed = byteSpeed;
	}

	public int getRecordSpeed() {
		return recordSpeed;
	}

	public void setRecordSpeed(int recordSpeed) {
		this.recordSpeed = recordSpeed;
	}

	public int getFlowControlInterval() {
		return flowControlInterval;
	}

	public void setFlowControlInterval(int flowControlInterval) {
		this.flowControlInterval = flowControlInterval;
	}

	public int getByteCapacity() {
		return byteCapacity;
	}

	public void setByteCapacity(int byteCapacity) {
		this.byteCapacity = byteCapacity;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
