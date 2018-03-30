package com.wetoop.storeoperator.bean;

/**
 * Created by Administrator on 2016/11/14.
 */
public class PriceBean {

    private String id;

    private String priceType;
    private int priceNum;

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public int getPriceNum() {
        return priceNum;
    }

    public void setPriceNum(int priceNum) {
        this.priceNum = priceNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
