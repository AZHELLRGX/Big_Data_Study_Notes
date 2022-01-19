package org.azhell.learn.flink.jointest;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.math.BigDecimal;

/**
 * 测试window join
 */
public class TestTumbleWindowJoin {
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
        1、Window Join首先需要使用where和equalTo指定使用哪个key来进行关联，此处通过应用方法，基于GoodsId来关联两个流中的元素。
        2、设置了5秒的滚动窗口，流的元素关联都会在这个5秒的窗口中进行关联。
        3、apply方法中实现了，将两个不同类型的元素关联并生成一个新类型的元素。
         */
        // 进行关联查询
        DataStream<FactOrderItem> factOrderItemDS = orderItemDS.join(goodsDS)
                // 第一个流orderItemDS
                .where(OrderItem::getGoodsId)
                // 第二流goodsDS
                .equalTo(Goods::getGoodsId)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .apply((OrderItem item, Goods goods) -> {
                    FactOrderItem factOrderItem = new FactOrderItem();
                    factOrderItem.setGoodsId(goods.getGoodsId());
                    factOrderItem.setGoodsName(goods.getGoodsName());
                    factOrderItem.setCount(new BigDecimal(item.getCount()));
                    factOrderItem.setTotalMoney(goods.getGoodsPrice().multiply(new BigDecimal(item.getCount())));

                    return factOrderItem;
                });

        factOrderItemDS.print();

        env.execute("滚动窗口JOIN");
    }
}
