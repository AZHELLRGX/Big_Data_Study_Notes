package cn.mastercom.backstage.dataxsync.core.transport;

public interface RecordSender<T> {

	T createRecord();

	void sendToWriter(T t);

	void flush();

	void terminate();

	void shutdown();
}
