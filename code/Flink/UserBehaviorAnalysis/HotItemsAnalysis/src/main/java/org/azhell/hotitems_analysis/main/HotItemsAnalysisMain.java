package org.azhell.hotitems_analysis.main;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.azhell.hotitems_analysis.beans.ItemViewCount;
import org.azhell.hotitems_analysis.beans.UserBehavior;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;

public class HotItemsAnalysisMain {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 时间语义
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // 读取数据
        DataStream<String> inputDs = env.readTextFile("D:\\work-space\\github\\Big_Data_Study_Notes\\resources\\Flink\\UserBehavior.csv");

        // source切换为Kafka
        // DataStream<String> inputDs = env.addSource(new FlinkKafkaConsumer<>("hotitems",new SimpleStringSchema(), initKafkaProperties()));

        // 转换为POJO，分配时间戳和watermark
        DataStream<UserBehavior> userBehaviorDs = inputDs.map(line -> {
            String[] fields = line.split(",");
            return new UserBehavior(fields);
        })
                // 因为数据是有序的，所以使用AscendingTimestampExtractor
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<UserBehavior>() {
                    @Override
                    public long extractAscendingTimestamp(UserBehavior element) {
                        return element.getTimestamp() * 1000L;
                    }
                });

        // 分组开窗聚合，得到每个窗口内各个商品的count值
        DataStream<ItemViewCount> itemViewCountDs =
                userBehaviorDs.filter(userBehavior ->
                        "pv".equals(userBehavior.getBehavior()))
                        .keyBy("itemId")
                        // 一小时窗口，滑动五分钟
                        .timeWindow(Time.hours(1), Time.minutes(5))
                        .aggregate(new ItemCountAgg(), new WindowItemCountResult());

        // 收集同一窗口的所有商品count数据，输出topN
        DataStream<String> topNDs = itemViewCountDs.keyBy("windowEnd") // 按照窗口分组
                .process(new TopNHotItems(5)); // 用自定义处理函数排序取前5热门商品

        topNDs.print();

        env.execute("HotItems Analysis");

    }


    /**
     * 初始化Kafka连接信息
     */
    private static Properties initKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "consumer-group");
        properties.setProperty("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("auto.offset.reset", "latest");
        return properties;
    }

    private static class TopNHotItems extends KeyedProcessFunction<Tuple, ItemViewCount, String> {
        private final Integer n;

        public TopNHotItems(Integer n) {
            this.n = n;
        }

        // 定义列表状态，保存当前窗口内所有输出的ItemViewCount
        // 这里有个异味，不知道加了private transient，是否存在负面影响
        // 参考书籍代码，人家也是这样写的，可以逐渐尝试一下
        private transient ListState<ItemViewCount> itemViewCountListState;

        @Override
        public void open(Configuration parameters) {
            itemViewCountListState = getRuntimeContext().getListState(
                    new ListStateDescriptor<>("item-view-count-list", ItemViewCount.class));
        }

        @Override
        public void processElement(ItemViewCount value, Context ctx, Collector<String> out) throws Exception {
            // 每来一条数据，存入list中，并注册定时器
            itemViewCountListState.add(value);
            // 这里并不会注册多个定时器，flink是按照时间戳来区分定时器的
            ctx.timerService().registerEventTimeTimer(value.getWindowEnd() + 1);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            // 定时器触发的时候，将当前已经收集到的所有数据，排序输出
            ArrayList<ItemViewCount> itemViewCounts = Lists.newArrayList(itemViewCountListState.get().iterator());
            itemViewCounts.sort(Comparator.comparingLong(ItemViewCount::getCount));

            // 将排名信息格式化成为String，方便打印
            StringBuilder sb = new StringBuilder();
            sb.append("===================\n")
                    .append("窗口结束时间：").append(new Date(timestamp - 1)) // 或者使用timestamp - 1
                    .append("\n");
            for (int i = 0; i < Math.min(itemViewCounts.size(), n); i++) {
                ItemViewCount itemViewCount = itemViewCounts.get(i);
                sb.append("No ").append(i + 1).append(":").append(" 商品id = ")
                        .append(itemViewCount.getItemId())
                        .append(" 热门度 = ").append(itemViewCount.getCount())
                        .append("\n");
            }
            sb.append("-----------------------------\n\n");
            // 控制输出频率
            Thread.sleep(1000L);

            out.collect(sb.toString());
        }
    }

    /**
     * 实现自定义增量聚合类
     */
    private static class ItemCountAgg implements AggregateFunction<UserBehavior, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(UserBehavior value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return a + b;
        }
    }

    /**
     * 实现自定义全窗口函数
     * 因为key直接使用字段名取出的，所以需要使用Tuple
     */
    private static class WindowItemCountResult implements WindowFunction<Long, ItemViewCount, Tuple, TimeWindow> {

        @Override
        public void apply(Tuple tuple, TimeWindow window, Iterable<Long> input, Collector<ItemViewCount> out) {
            Long itemId = tuple.getField(0);
            Long windowEnd = window.getEnd();
            Long count = input.iterator().next();
            out.collect(new ItemViewCount(itemId, windowEnd, count));
        }
    }
}
