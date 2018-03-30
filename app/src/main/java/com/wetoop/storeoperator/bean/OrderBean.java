package com.wetoop.storeoperator.bean;


import java.util.Date;

/**
 * Created by Administrator on 2015/9/14.
 */
public class OrderBean {
    private String id;

    private String title;

    private Double totalPrice;

    private String fundsAdjust;

    private Date createdAt;

    private Date purchasedAt;

    private Date cancelledAt;

    private String mobile;

    private String address;

    private String used;

    private String customer_note;

    private String refunded;

    private String bonus_price;

    public void setId(String id){
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setTotalPrice(Double totalPrice){
        this.totalPrice = totalPrice;
    }
    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setFundsAdjust(String fundsAdjust){
        this.fundsAdjust = fundsAdjust;
    }
    public String getFundsAdjust() {
        return fundsAdjust;
    }

    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setPurchasedAt(Date purchasedAt){
        this.purchasedAt = purchasedAt;
    }
    public Date getPurchasedAt() {
        return purchasedAt;
    }

    public  void setCancelledAt(Date cancelledAt){
        this.cancelledAt = cancelledAt;
    }
    public Date getCancelledAt() {
        return cancelledAt;
    }

    public void setMobile(String mobile){
        this.mobile = mobile;
    }
    public String getMobile() {
        return mobile;
    }

    public void setAddress(String address){
        this.address = address;
    }
    public String getAddress() {
        return address;
    }

    public void setUsed(String used){
        this.used = used;
    }
    public String getUsed() {
        return used;
    }

    public void setCustomer_note(String customer_note){
        this.customer_note = customer_note;
    }
    public String getCustomer_note() {
        return customer_note;
    }

    public String getRefunded() {
        return refunded;
    }
    public void setRefunded(String refunded) {
        this.refunded = refunded;
    }

    public String getBonus_price() {
        return bonus_price;
    }

    public void setBonus_price(String bonus_price) {
        this.bonus_price = bonus_price;
    }
}
