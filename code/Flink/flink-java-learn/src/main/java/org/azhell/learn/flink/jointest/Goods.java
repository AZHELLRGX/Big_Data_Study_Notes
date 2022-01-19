package org.azhell.learn.flink.jointest;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 商品实体
 */
public class Goods {
    private String goodsId;
    private String goodsName;
    private BigDecimal goodsPrice;

    protected static final List<Goods> GOODS_LIST;
    public static final Random r;

    static {
        r = new Random();

        GOODS_LIST = new ArrayList<>();

        GOODS_LIST.add(new Goods("1", "小米12", new BigDecimal(4890)));
        GOODS_LIST.add(new Goods("2", "iphone12", new BigDecimal(12000)));
        GOODS_LIST.add(new Goods("3", "MacBookPro", new BigDecimal(15000)));
        GOODS_LIST.add(new Goods("4", "Thinkpad X1", new BigDecimal(9800)));
        GOODS_LIST.add(new Goods("5", "MeiZu One", new BigDecimal(3200)));
        GOODS_LIST.add(new Goods("6", "Mate 40", new BigDecimal(6500)));
    }

    public static Goods randomGoods() {
        int rIndex = r.nextInt(GOODS_LIST.size());

        return GOODS_LIST.get(rIndex);
    }

    public Goods() {
    }

    public Goods(String goodsId, String goodsName, BigDecimal goodsPrice) {
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.goodsPrice = goodsPrice;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }


    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    // 生成getter/setter此处省略

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
