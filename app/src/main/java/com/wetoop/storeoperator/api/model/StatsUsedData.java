package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/15.
 */
public class StatsUsedData {
    @SerializedName("name")
    private String userName;
    @SerializedName("errorCode")
    private String errorCode;
    @SerializedName("errorMessage")
    private String errorMessage;
    @SerializedName("sum_display")
    private boolean sumDisplay;
    @SerializedName("list")
    private ArrayList<Order> list;

    public String getUserName() {
        return userName;
    }

    public ArrayList<Order> getList() {
        return list;
    }

    public boolean isSumDisplay() {
        return sumDisplay;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
