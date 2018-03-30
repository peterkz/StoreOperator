package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bruce on 15-4-25.
 */
public class ResultMessage {
    @SerializedName("errorCode")
    private int errorCode;

    @SerializedName("errorMessage")
    private String errorMessage;

    @SerializedName("result")
    private String result;

    @SerializedName("title")
    private String message;

    @SerializedName("name")
    private String name;

    @SerializedName("allow_pay")
    private String allow_pay;

    @SerializedName("order_id")
    private String order_id;

    @SerializedName("order_no")
    private String order_no;

    @SerializedName("request_another")
    private boolean request_another;

    @SerializedName("id")
    private String user_id;

    @SerializedName("allow_show_list")
    private String allow_show_list;

    public int getErrorCode() {
        return errorCode;
    }

    public String getOrder_no() {
        return order_no;
    }

    public boolean getRequest_another() {
        return request_another;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getAllow_pay() {
        return allow_pay;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getAllow_show_list() {
        return allow_show_list;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

}
