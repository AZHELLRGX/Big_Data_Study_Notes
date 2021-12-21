package cn.mastercom.backstage.dataxsync.core.transport;

import cn.mastercom.backstage.dataxsync.config.ChannelConfig;
import cn.mastercom.backstage.dataxsync.core.exception.ChannelException;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.CommonErrorCode;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.core.transport.record.DefaultRecord;
import com.alibaba.datax.core.transport.record.TerminateRecord;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferedRecordExchanger implements RecordSender<Record>, RecordReceiver<Record> {

	private final Channel channel;

	private final ChannelConfig channelConfig;

	private final List<Record> buffer;

	private int bufferSize;

	protected final int byteCapacity;

	private final AtomicInteger memoryBytes = new AtomicInteger(0);

	private int bufferIndex = 0;

	private volatile boolean shutdown = false;

	public BufferedRecordExchanger(final Channel channel) throws ChannelException {

		if (null == channel || null == channel.getChannelConfig()) {
			throw new ChannelException("channel is null or channel config is null");
		}

		this.channel = channel;
		this.channelConfig = channel.getChannelConfig();

		this.bufferSize = channelConfig.getBufferSize();
		this.buffer = new ArrayList<>(bufferSize);

		// channel的queue默认大小为8M，原来为64M
		this.byteCapacity = channelConfig.getByteCapacity();
	}

	@Override
	public Record createRecord() {
		try {
			return new DefaultRecord();
		} catch (Exception e) {
			throw DataXException.asDataXException(
					FrameworkErrorCode.CONFIG_ERROR, e);
		}
	}

	@Override
	public void sendToWriter(Record record) {
		if (shutdown) {
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}

		Validate.notNull(record, "record不能为空.");

		if (record.getMemorySize() > this.byteCapacity) {
			return;
		}

		boolean isFull = (this.bufferIndex >= this.bufferSize || this.memoryBytes.get() + record.getMemorySize() > this.byteCapacity);
		if (isFull) {
			flush();
		}

		this.buffer.add(record);
		this.bufferIndex++;
		memoryBytes.addAndGet(record.getMemorySize());
	}

	@Override
	public void flush() {
		if (shutdown) {
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		this.channel.pushAll(this.buffer);
		this.buffer.clear();
		this.bufferIndex = 0;
		this.memoryBytes.set(0);
	}

	@Override
	public void terminate() {
		if (shutdown) {
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		flush();
		this.channel.pushTerminate(TerminateRecord.get());
	}

	@Override
	public Record getFromReader() {
		if (shutdown) {
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		boolean isEmpty = (this.bufferIndex >= this.buffer.size());
		if (isEmpty) {
			receive();
		}

		Record record = this.buffer.get(this.bufferIndex++);
		if (record instanceof TerminateRecord) {
			record = null;
		}
		return record;
	}

	@Override
	public void shutdown() {
		shutdown = true;
		try {
			buffer.clear();
			channel.clear();
		} catch (Exception t) {
			//
		}
	}

	private void receive() {
		this.channel.pullAll(this.buffer);
		this.bufferIndex = 0;
		this.bufferSize = this.buffer.size();
	}
}
