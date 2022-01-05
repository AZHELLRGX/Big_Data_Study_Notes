package org.azhell.learn.flink.apitest.state;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.util.Collector;
import org.azhell.learn.flink.tool.Utils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述(description)：使用广播流实现配置的动态更新
 */
public class BroadcastStreamDemo {

    public static void main(String[] args) throws Exception {

        // 构建流处理环境
        final StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        // 配置处理环境的并发度为4，测试使用
        environment.setParallelism(4);

        // 状态描述
        final MapStateDescriptor<String, String> configKeywords = new MapStateDescriptor<>(
                "config-keywords",
                BasicTypeInfo.STRING_TYPE_INFO,
                BasicTypeInfo.STRING_TYPE_INFO);

        // 自定义广播流（并行度1）
        BroadcastStream<String> broadcastStream = environment.addSource(new RichSourceFunction<String>() {

            private volatile boolean isRunning = true;
            //测试数据集
            private final String[] dataSet = new String[]{
                    "java",
                    "swift",
                    "php",
                    "go",
                    "python"
            };
            private final Random random = new Random();

            /**
             * 数据源：模拟每30秒随机更新一次拦截的关键字
             */
            @Override
            public void run(SourceContext<String> ctx) throws Exception {
                int size = dataSet.length;
                while (isRunning) {
                    TimeUnit.SECONDS.sleep(30);
                    int seed = random.nextInt(size);
                    //随机选择关键字发送
                    ctx.collect(dataSet[seed]);
                    Utils.print("读取到上游发送的关键字:{}", dataSet[seed]);
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        }).setParallelism(1).broadcast(configKeywords);

        // 自定义数据流（并行度1）
        DataStream<String> dataStream = environment.addSource(new RichSourceFunction<String>() {

            private volatile boolean isRunning = true;

            //测试数据集
            private final String[] dataSet = new String[]{
                    "java是世界上最优秀的语言",
                    "swift是世界上最优秀的语言",
                    "php是世界上最优秀的语言",
                    "go是世界上最优秀的语言",
                    "python是世界上最优秀的语言"
            };
            private final Random random = new Random();

            /**
             * 模拟每3秒随机产生1条消息
             */
            @Override
            public void run(SourceContext<String> ctx) throws Exception {
                int size = dataSet.length;
                while (isRunning) {
                    TimeUnit.SECONDS.sleep(1);
                    int seed = random.nextInt(size);
                    ctx.collect(dataSet[seed]);
                    Utils.print("读取到上游发送的消息：{}", dataSet[seed]);
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }

        });

        // 数据流和广播流连接处理并将拦截结果打印
        dataStream.connect(broadcastStream).process(new BroadcastProcessFunction<String, String, String>() {

            //拦截的关键字
            private String keywords = null;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                keywords = "java";
                Utils.print("初始化模拟连接数据库读取拦截关键字：java");
            }

            @Override
            public void processElement(String value, ReadOnlyContext ctx, Collector<String> out) {
                if (value.contains(keywords)) {
                    out.collect("拦截消息:" + value + ", 原因:包含拦截关键字：" + keywords);
                }
            }

            @Override
            public void processBroadcastElement(String value, Context ctx, Collector<String> out) {
                keywords = value;
                Utils.print("关键字更新成功，更新拦截关键字：{}", value);
            }
        }).print();

        // 懒加载执行
        environment.execute();
    }

}

