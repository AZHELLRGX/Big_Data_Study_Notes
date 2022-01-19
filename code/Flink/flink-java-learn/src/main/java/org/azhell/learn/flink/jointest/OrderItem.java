package org.azhell.learn.flink.jointest;

import com.alibaba.fastjson.JSON;

/**
 * 订单实体
 */
public class OrderItem {
    private String itemId;
    private String goodsId;
    private Integer count;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}


