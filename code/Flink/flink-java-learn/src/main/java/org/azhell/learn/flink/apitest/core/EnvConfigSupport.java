package org.azhell.learn.flink.apitest.core;

import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * Flink环境配置Api
 */
public class EnvConfigSupport {
    public void setEnvConfig(StreamExecutionEnvironment env) {
        final CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        // 默认情况下，Checkpoint机制是关闭的
        env.enableCheckpointing(Duration.ofMinutes(5).toMillis());

        // 设置三种不同的State Backend
        // 旧版本设置MemoryStateBackend，默认内存大小是5M
        // env.setStateBackend(new MemoryStateBackend(MemoryStateBackend.DEFAULT_MAX_STATE_SIZE))
        // 新版本需要使用HashMapStateBackend
        env.setStateBackend(new HashMapStateBackend());
        // 设置FsStateBackend 已经过时
        // env.setStateBackend(new FsStateBackend("hdfs:///usr/checkpoint"))
        // 旧版本的 FsStateBackend 等价于使用 HashMapStateBackend 和 FileSystemCheckpointStorage
        checkpointConfig.setCheckpointStorage(new FileSystemCheckpointStorage("hdfs:///usr/checkpoint"));
        // 当前版本直接设置RocksDBStateBackend已经过时，使用如下方案代替  可以设置参数来决定是否开启增量快照
        env.setStateBackend(new EmbeddedRocksDBStateBackend(true));
        // 内部也是调用的FileSystemCheckpointStorage
        checkpointConfig.setCheckpointStorage("hdfs:///usr/checkpoint");
        // 关于状态后端的更多配置【内存管理优化等】可以参考官方文档 https://nightlies.apache.org/flink/flink-docs-master/zh/docs/ops/state/state_backends/

        // 设置Flink的Checkpoint Barrier跳过对齐机制
        checkpointConfig.enableUnalignedCheckpoints();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // 如果一次Checkpoint超过规定时间仍未完成，直接将其终止，以免其占用太多资源。
        checkpointConfig.setCheckpointTimeout(Duration.ofHours(1).toMillis());
        // 设置该参数为60秒，那么前一次Checkpoint结束后，60秒内不会启动新的Checkpoint。这种方式只在整个作业最多允许1个Checkpoint时适用。
        checkpointConfig.setMinPauseBetweenCheckpoints(Duration.ofMillis(60).toMillis());
        // 默认情况下，一个作业只允许1个Checkpoint执行，如果某个Checkpoint正在进行，另外一个Checkpoint被启动，新的Checkpoint需要挂起等待。
        // 如果该参数大于1，将与前面提到的最短间隔相冲突
        checkpointConfig.setMaxConcurrentCheckpoints(3);
        // 默认flink在作业执行失败后会删除远程存储空间上的数据
        // 如果开发者希望通过Checkpoint数据进行调试，自己关停了作业，同时希望将远程数据保存下来，需要设置代码如下
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 默认情况下，如果Checkpoint过程失败，会导致整个应用重启。我们可以关闭这个功能，这样Checkpoint过程失败不会影响作业的执行
        // checkpointConfig.setFailOnCheckpointingErrors(false)
        // 以上方法已经过时，可以使用如下方法，参数表示最多可以容忍多少次checkpoint失败
        checkpointConfig.setTolerableCheckpointFailureNumber(10);
    }
}
