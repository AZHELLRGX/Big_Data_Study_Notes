package org.azhell.learn.flink.jointest;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.concurrent.TimeUnit;

public class GoodsSourceFunction extends RichSourceFunction<Goods> {

    private boolean isCancel;

    @Override
    public void open(Configuration parameters) {
        isCancel = false;
    }

    @Override
    public void run(SourceContext<Goods> sourceContext) throws Exception {
        while (!isCancel) {
            Goods.GOODS_LIST.forEach(sourceContext::collect);
            // 模拟流
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Override
    public void cancel() {
        isCancel = true;
    }
}
