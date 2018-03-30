package com.wetoop.storeoperator.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bruce on 15-4-30.
 */
public class Order implements Parcelable {
    @SerializedName("order_no")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("switch_id")
    private String switch_id;

    @SerializedName("price")
    private Double totalPrice;

    @SerializedName("funds")
    private String fundsAdjust;

    @SerializedName("created")
    private Date createdAt;

    @SerializedName("purchased")
    private Date purchasedAt;

    @SerializedName("cancelled")
    private Date cancelledAt;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("treated")
    private String treated;

    @SerializedName("address")
    private String address;

    @SerializedName("used")
    private String used;

    @SerializedName("customer_note")
    private String customer_note;

    @SerializedName("refunded")
    private String refunded;

    @SerializedName("bonus_price")
    private String bonus_price;

    @SerializedName("coupon_price")
    private String coupon_price;//优惠折扣

    @SerializedName("operator")
    private String operator;//核销人员

    @SerializedName("booking_at")
    private String booking_at;//预约日期/时间

    @SerializedName("errorCode")
    private String errorCode;//401:登录过期

    @SerializedName("errorMessage")
    private String errorMessage;//errorCode为空时它也为空

    @SerializedName("request_another")
    private boolean request_another;

    @SerializedName("details")
    private ArrayList<DetailsData> details;

    @SerializedName("mandatory")
    private boolean mandatory;

    @SerializedName("switch_auto")
    private boolean switch_auto;

    public String getCustomer_note() {
        return customer_note;
    }

    public String getUsed() {
        return used;
    }

    public String getAddress() {
        return address;
    }

    public String getTreated() {
        return treated;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSwitch_id() {
        return switch_id;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getFundsAdjust() {
        return fundsAdjust;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getPurchasedAt() {
        return purchasedAt;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public String getMobile() {
        return mobile;
    }

    public String getRefunded() {
        return refunded;
    }

    public String getBonus_price() {
        return bonus_price;
    }

    public String getCoupon_price() {
        return coupon_price;
    }

    public String getOperator() {
        return operator;
    }

    public String getBooking_at() {
        return booking_at;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRequest_another() {
        return request_another;
    }

    public ArrayList<DetailsData> getDetails() {
        return details;
    }

    public boolean getMandatory() {
        return mandatory;
    }

    public boolean getSwitch_auto() {
        return switch_auto;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        //if(totalPrice!=null)
        dest.writeDouble(totalPrice);
        dest.writeString(fundsAdjust);
        dest.writeSerializable(createdAt);
        dest.writeSerializable(purchasedAt);
        dest.writeSerializable(cancelledAt);
        dest.writeString(mobile);
        dest.writeString(address);
        dest.writeString(used);
        dest.writeString(customer_note);
        dest.writeString(refunded);
        dest.writeString(bonus_price);
        //if(coupon_price!=null)
        dest.writeString(coupon_price);
        dest.writeString(operator);
        if (booking_at != null)
            dest.writeString(booking_at);
        if (switch_id != null)
            dest.writeString(switch_id);
        if (errorCode != null)
            dest.writeString(errorCode);
        if (errorMessage != null)
            dest.writeString(errorMessage);
        //dest.writeList(details);
        //dest.writeString(treated);
    }

    private void readFromParcel(Parcel in) {
        id = in.readString();
        title = in.readString();
        totalPrice = in.readDouble();
        fundsAdjust = in.readString();
        createdAt = (Date) in.readSerializable();
        purchasedAt = (Date) in.readSerializable();
        cancelledAt = (Date) in.readSerializable();
        mobile = in.readString();
        address = in.readString();
        used = in.readString();
        customer_note = in.readString();
        refunded = in.readString();
        bonus_price = in.readString();
        coupon_price = in.readString();
        operator = in.readString();
        if (booking_at != null)
            booking_at = in.readString();
        if (switch_id != null)
            switch_id = in.readString();
        if (errorCode != null)
            errorCode = in.readString();
        if (errorMessage != null)
            errorMessage = in.readString();
        //details = in.readList(details,);
        //treated = in.readString();
    }

    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    private Order(Parcel in) {
        readFromParcel(in);
    }

    public OrderStatus getStatus() {
        if (this.getPurchasedAt() != null) { // 付款时间
            if (!TextUtils.isEmpty(this.getRefunded())) { // 退款时间
                return OrderStatus.REFUNDED;
            }
            if (!TextUtils.isEmpty(this.getUsed())) { // 使用时间
                return OrderStatus.USED;
            }
            return OrderStatus.PAID;
        }
        if (this.getCancelledAt() != null) { // 取消时间
            return OrderStatus.CANCELLED;
        }
        if (this.getCreatedAt() != null) { // 下单时间
            return OrderStatus.CREATED;
        }
        return OrderStatus.CREATED;
    }

    public enum OrderStatus {
        CREATED("待付款"), PAID("已支付"), REFUNDED("已退款"), CANCELLED("已取消"), USED("已使用");

        private String name;

        OrderStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
