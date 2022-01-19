package org.azhell.learn.flink.jointest;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

/**
 * 测试 internal join
 */
public class TestIntervalJoin {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 构建商品数据流
        SingleOutputStreamOperator<Goods> goodsDS = env.addSource(new GoodsSourceFunction(), TypeInformation.of(Goods.class))
                .assignTimestampsAndWatermarks(new GoodsWatermark() {
                });
        // 构建订单明细数据流
        SingleOutputStreamOperator<OrderItem> orderItemDS = env.addSource(new OrderItemSourceFunction(), TypeInformation.of(OrderItem.class))
                .assignTimestampsAndWatermarks(new OrderItemWatermark());

        /*
        1、这里通过keyBy将两个流join到一起
        2、interval join需要设置流A去关联哪个时间范围的流B中的元素。此处，我设置的下界为-1、上界为0，且上界是一个开区间。表达的意思就是流A中某个元素的时间，对应上一秒的流B中的元素。
        3、process中将两个key一样的元素，关联在一起，并加载到一个新的FactOrderItem对象中
         */
        // 进行关联查询
        SingleOutputStreamOperator<FactOrderItem> factOrderItemDS =
                orderItemDS.keyBy(OrderItem::getGoodsId)
                        .intervalJoin(goodsDS.keyBy(Goods::getGoodsId))
                        // 设置时间上下限(先设置下限，而后设置上限)
                        .between(Time.seconds(-1), Time.seconds(0))
                        .upperBoundExclusive()
                        .process(new ProcessJoinFunction<OrderItem, Goods, FactOrderItem>() {
                            @Override
                            public void processElement(OrderItem left, Goods right, Context ctx, Collector<FactOrderItem> out) throws Exception {
                                FactOrderItem factOrderItem = new FactOrderItem();
                                factOrderItem.setGoodsId(right.getGoodsId());
                                factOrderItem.setGoodsName(right.getGoodsName());
                                factOrderItem.setCount(new BigDecimal(left.getCount()));
                                factOrderItem.setTotalMoney(right.getGoodsPrice().multiply(new BigDecimal(left.getCount())));

                                out.collect(factOrderItem);
                            }
                        });

        factOrderItemDS.print();

        env.execute("Interval JOIN");
    }
}

