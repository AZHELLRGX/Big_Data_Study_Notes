package org.azhell.learn.flink.jointest;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;

public class FactOrderItem {

    private String goodsId;
    private String goodsName;
    private BigDecimal count;
    private BigDecimal totalMoney;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    // 生成的getter/setter


    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public BigDecimal getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(BigDecimal totalMoney) {
        this.totalMoney = totalMoney;
    }
}
