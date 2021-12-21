package cn.mastercom.backstage.dataxsync.core.transport;

import cn.mastercom.backstage.dataxsync.config.ChannelConfig;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.core.statistics.communication.Communication;
import com.alibaba.datax.core.statistics.communication.CommunicationTool;
import com.alibaba.datax.core.transport.record.TerminateRecord;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jingxing on 14-8-25.
 * <p/>
 * 统计和限速都在这里
 */
public abstract class Channel {

	private static final Logger LOG = LoggerFactory.getLogger(Channel.class);

	protected int taskGroupId;

	protected int capacity;

	protected int byteCapacity;

	protected long byteSpeed; // bps: bytes/s

	protected long recordSpeed; // tps: records/s

	protected long flowControlInterval;

	protected volatile boolean isClosed = false;

	protected ChannelConfig channelConfig;

	protected AtomicLong waitReaderTime = new AtomicLong(0);

	protected AtomicLong waitWriterTime = new AtomicLong(0);

	private Communication currentCommunication;

	private Communication lastCommunication = new Communication();

	public Channel(final ChannelConfig channelConfig) {
		// channel的queue里默认record为1万条。原来为512条
		int capacityLocal = channelConfig.getCapacity();
		long byteSpeedLocal = channelConfig.getByteSpeed();
		long recordSpeedLocal = channelConfig.getRecordSpeed();

		if (capacityLocal <= 0) {
			throw new IllegalArgumentException(String.format(
					"通道容量[%d]必须大于0.", capacityLocal));
		}

		LOG.info("Channel set byte_speed_limit to {}", byteSpeedLocal);
		LOG.info("Channel set record_speed_limit to {}", recordSpeedLocal);

		this.capacity = capacityLocal;
		this.byteSpeed = byteSpeedLocal;
		this.recordSpeed = recordSpeedLocal;
		this.flowControlInterval = channelConfig.getFlowControlInterval();
		// channel的queue默认大小为8M，原来为64M
		this.byteCapacity = channelConfig.getByteCapacity();
		this.channelConfig = channelConfig;
	}

	public ChannelConfig getChannelConfig() {
		return channelConfig;
	}

	public void close() {
		this.isClosed = true;
	}

	public void open() {
		this.isClosed = false;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int getTaskGroupId() {
		return this.taskGroupId;
	}

	public int getCapacity() {
		return capacity;
	}

	public long getByteSpeed() {
		return byteSpeed;
	}

	public void setCommunication(final Communication communication) {
		this.currentCommunication = communication;
		this.lastCommunication.reset();
	}

	public void push(final Record r) {
		Validate.notNull(r, "record不能为空.");
		this.doPush(r);
		this.statPush(1L, r.getByteSize());
	}

	public void pushTerminate(final TerminateRecord r) {
		Validate.notNull(r, "record不能为空.");
		this.doPush(r);
	}

	public void pushAll(final Collection<Record> rs) {
		Validate.notNull(rs);
		Validate.noNullElements(rs);
		this.doPushAll(rs);
		this.statPush(rs.size(), this.getByteSize(rs));
	}

	public Record pull() {
		Record record = this.doPull();
		this.statPull(1L, record.getByteSize());
		return record;
	}

	public void pullAll(final Collection<Record> rs) {
		Validate.notNull(rs);
		this.doPullAll(rs);
		this.statPull(rs.size(), this.getByteSize(rs));
	}

	protected abstract void doPush(Record r);

	protected abstract void doPushAll(Collection<Record> rs);

	protected abstract Record doPull();

	protected abstract void doPullAll(Collection<Record> rs);

	public abstract int size();

	public abstract boolean isEmpty();

	public abstract void clear();

	private long getByteSize(final Collection<Record> rs) {
		long size = 0;
		for (final Record each : rs) {
			size += each.getByteSize();
		}
		return size;
	}

	private void statPush(long recordSize, long byteSize) {
		currentCommunication.increaseCounter(CommunicationTool.READ_SUCCEED_RECORDS,
				recordSize);
		currentCommunication.increaseCounter(CommunicationTool.READ_SUCCEED_BYTES,
				byteSize);
		// 在读的时候进行统计waitCounter即可，因为写（pull）的时候可能正在阻塞，但读的时候已经能读到这个阻塞的counter数

		currentCommunication.setLongCounter(CommunicationTool.WAIT_READER_TIME, waitReaderTime.get());
		currentCommunication.setLongCounter(CommunicationTool.WAIT_WRITER_TIME, waitWriterTime.get());

		boolean isChannelByteSpeedLimit = (this.byteSpeed > 0);
		boolean isChannelRecordSpeedLimit = (this.recordSpeed > 0);
		if (!isChannelByteSpeedLimit && !isChannelRecordSpeedLimit) {
			return;
		}

		long lastTimestamp = lastCommunication.getTimestamp();
		long nowTimestamp = System.currentTimeMillis();
		long interval = nowTimestamp - lastTimestamp;
		if (interval - this.flowControlInterval >= 0) {
			long byteLimitSleepTime = calByteLimitSleepTime(isChannelByteSpeedLimit, interval);
			long recordLimitSleepTime = calByteLimitSleepTime(isChannelRecordSpeedLimit, interval);

			// 休眠时间取较大值
			long sleepTime = byteLimitSleepTime < recordLimitSleepTime ? recordLimitSleepTime : byteLimitSleepTime;
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			lastCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_BYTES,
					currentCommunication.getLongCounter(CommunicationTool.READ_SUCCEED_BYTES));
			lastCommunication.setLongCounter(CommunicationTool.READ_FAILED_BYTES,
					currentCommunication.getLongCounter(CommunicationTool.READ_FAILED_BYTES));
			lastCommunication.setLongCounter(CommunicationTool.READ_SUCCEED_RECORDS,
					currentCommunication.getLongCounter(CommunicationTool.READ_SUCCEED_RECORDS));
			lastCommunication.setLongCounter(CommunicationTool.READ_FAILED_RECORDS,
					currentCommunication.getLongCounter(CommunicationTool.READ_FAILED_RECORDS));
			lastCommunication.setTimestamp(nowTimestamp);
		}
	}

	public long calByteLimitSleepTime(boolean isChannelByteSpeedLimit, long interval) {
		if (isChannelByteSpeedLimit) {
			long currentByteSpeed = (CommunicationTool.getTotalReadBytes(currentCommunication) -
					CommunicationTool.getTotalReadBytes(lastCommunication)) * 1000 / interval;
			if (currentByteSpeed > this.byteSpeed) {
				// 计算根据byteLimit得到的休眠时间
				return currentByteSpeed * interval / this.byteSpeed
						- interval;
			}
		}
		return 0;
	}

	public long calRecordLimitSleepTime(boolean isChannelRecordSpeedLimit, long interval) {
		if (isChannelRecordSpeedLimit) {
			long currentRecordSpeed = (CommunicationTool.getTotalReadRecords(currentCommunication) -
					CommunicationTool.getTotalReadRecords(lastCommunication)) * 1000 / interval;
			if (currentRecordSpeed > this.recordSpeed) {
				// 计算根据recordLimit得到的休眠时间
				return currentRecordSpeed * interval / this.recordSpeed
						- interval;
			}
		}
		return 0;
	}

	private void statPull(long recordSize, long byteSize) {
		currentCommunication.increaseCounter(
				CommunicationTool.WRITE_RECEIVED_RECORDS, recordSize);
		currentCommunication.increaseCounter(
				CommunicationTool.WRITE_RECEIVED_BYTES, byteSize);
	}

}
