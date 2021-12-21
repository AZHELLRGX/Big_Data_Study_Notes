
package cn.mastercom.backstage.dataxsync.core.transport;

public interface  RecordReceiver<T> {

	T getFromReader();

	void shutdown();
}
