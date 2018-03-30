package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by WETOOP on 2018/3/7.
 */

public class UserInfo extends ResultMessage {

    @SerializedName("printer_service")
    private String[] printer_service;

    @SerializedName("card_prefix")
    private String[] card_prefix;

    public String[] getPrinter_service() {
        return printer_service;
    }

    public String[] getCard_prefix() {
        return card_prefix;
    }
}
