package org.azhell.networkflow_analysis.main;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.azhell.networkflow_analysis.beans.ApacheLogEvent;
import org.azhell.networkflow_analysis.beans.PageViewCount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Pattern;

public class HotPagesNetworkFlowMain {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置时间语义
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStreamSource<String> sourceDs = env.readTextFile("D:\\work-space\\github\\Big_Data_Study_Notes\\resources\\Flink\\apache.log");

        // 因为需要改变时间格式，需要simpleDateFormat，理想情况是open的时候创建
        SingleOutputStreamOperator<ApacheLogEvent> apacheLogEventDs =
                sourceDs.map(new TransferApacheLogEventFunction())
                        // 针对乱序数据，分配时间戳和watermark，watermark延迟一秒
                        .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<ApacheLogEvent>(Time.seconds(1)) {
                            @Override
                            public long extractTimestamp(ApacheLogEvent element) {
                                return element.getTimestamp();
                            }
                        });


        // 只处理GET、并且过滤资源类型的请求
        SingleOutputStreamOperator<PageViewCount> aggregateDs = apacheLogEventDs.filter(element -> "GET".equals(element.getMethod()) && Pattern.matches("^((?!\\.(css|js|png|ico)$).)*$", element.getUrl()))
                .keyBy(ApacheLogEvent::getUrl)
                // 窗口大小、滑动步长
                .timeWindow(Time.minutes(10), Time.seconds(5))
                // 迟到数据不直接删除，侧路输出
                .allowedLateness(Time.minutes(1))
                .sideOutputLateData(new OutputTag<ApacheLogEvent>("late") {
                })
                .aggregate(new ItemCountAgg(), new WindowItemCountResult());

        // 收集同一窗口的所有信息，找出top n并且输出
        aggregateDs.keyBy(PageViewCount::getWindowEnd)
                .process(new TopNPagesNetworkFlow(5)).print();

        aggregateDs.getSideOutput(new OutputTag<ApacheLogEvent>("late") {}).print("late");


        env.execute("network flow");
    }

    private static class TransferApacheLogEventFunction extends RichMapFunction<String, ApacheLogEvent> {

        private SimpleDateFormat simpleDateFormat;

        @Override
        public void open(Configuration parameters) {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
        }

        @Override
        public ApacheLogEvent map(String value) throws Exception {
            String[] fields = value.split(" ");
            Long timestamp = simpleDateFormat.parse(fields[3]).getTime();
            return new ApacheLogEvent(fields[0], fields[1], timestamp, fields[5],
                    fields[6]);
        }
    }

    private static class ItemCountAgg implements AggregateFunction<ApacheLogEvent, Long, Long> {
        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(ApacheLogEvent value, Long accumulator) {
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

    private static class WindowItemCountResult implements WindowFunction<Long, PageViewCount, String, TimeWindow> {
        @Override
        public void apply(String s, TimeWindow window, Iterable<Long> input, Collector<PageViewCount> out) {
            Long count = input.iterator().next();
            out.collect(new PageViewCount(s, window.getEnd(), count));
        }
    }

    private static class TopNPagesNetworkFlow extends KeyedProcessFunction<Long, PageViewCount, String> {
        private final int n;

        private transient ListState<PageViewCount> listState;

        public TopNPagesNetworkFlow(int n) {
            this.n = n;
        }

        // 定时器触发的时候，做什么操作
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            ArrayList<PageViewCount> pageViewCounts = Lists.newArrayList(listState.get().iterator());
            pageViewCounts.sort(Comparator.comparingLong(PageViewCount::getCount));

            StringBuilder sb = new StringBuilder();
            sb.append("===================\n")
                    .append("窗口结束时间：").append(new Date(timestamp - 1)) // 或者使用timestamp - 1
                    .append("\n");
            for (int i = 0; i < Math.min(pageViewCounts.size(), n); i++) {
                PageViewCount pageViewCount = pageViewCounts.get(i);
                sb.append("No ").append(i + 1).append(":").append(" URL = ")
                        .append(pageViewCount.getUrl())
                        .append(" 热门度 = ").append(pageViewCount.getCount())
                        .append("\n");
            }
            sb.append("-----------------------------\n\n");
            // 控制输出频率
            Thread.sleep(1000L);

            out.collect(sb.toString());
        }

        @Override
        public void open(Configuration parameters) {
            this.listState = getRuntimeContext().getListState(
                    new ListStateDescriptor<>("page items count state", PageViewCount.class));
        }

        // 每条数据进来的时候的处理逻辑
        @Override
        public void processElement(PageViewCount value, Context ctx, Collector<String> out) throws Exception {
            listState.add(value);
            ctx.timerService().registerEventTimeTimer(value.getWindowEnd() + 1);
        }
    }
}
