package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bruce on 15-4-25.
 */
public class AccountResultMessage {
    @SerializedName("errorCode")
    private Integer errorCode;

    @SerializedName("errorMessage")
    private String errorMessage;

    @SerializedName("result")
    private Account result;

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Account getResult() {
        return result;
    }
}
