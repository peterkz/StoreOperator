package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bruce on 15-4-27.
 */
public class Account {
    @SerializedName("username")
    private String userName;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("email")
    private String email;

    public String getUserName() {
        return userName;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }
}
