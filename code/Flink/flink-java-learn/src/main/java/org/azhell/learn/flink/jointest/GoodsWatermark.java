package org.azhell.learn.flink.jointest;

import org.apache.flink.api.common.eventtime.*;

/**
 * 构建水印分配器，使用系统时间
 */
public class GoodsWatermark implements WatermarkStrategy<Goods> {

    @Override
    public TimestampAssigner<Goods> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return (element, recordTimestamp) -> System.currentTimeMillis();
    }

    @Override
    public WatermarkGenerator<Goods> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new WatermarkGenerator<Goods>() {
            @Override
            public void onEvent(Goods event, long eventTimestamp, WatermarkOutput output) {
                output.emitWatermark(new Watermark(System.currentTimeMillis()));
            }

            @Override
            public void onPeriodicEmit(WatermarkOutput output) {
                output.emitWatermark(new Watermark(System.currentTimeMillis()));
            }
        };
    }
}
