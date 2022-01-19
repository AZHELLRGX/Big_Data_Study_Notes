package org.azhell.learn.flink.jointest;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OrderItemSourceFunction extends RichSourceFunction<OrderItem> {

    private boolean isCancel;
    private Random r;

    @Override
    public void open(Configuration parameters) {
        isCancel = false;
        r = new Random();
    }

    @Override
    public void run(SourceContext<OrderItem> sourceContext) throws Exception {
        while (!isCancel) {
            Goods goods = Goods.randomGoods();
            OrderItem orderItem = new OrderItem();
            orderItem.setGoodsId(goods.getGoodsId());
            orderItem.setCount(r.nextInt(10) + 1);
            orderItem.setItemId(UUID.randomUUID().toString());

            // 下面模拟两种数据，一种是带有商品名称的，一种不带
            sourceContext.collect(orderItem);

            orderItem.setGoodsId("1");
            sourceContext.collect(orderItem);

            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
    }
}
