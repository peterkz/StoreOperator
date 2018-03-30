package com.wetoop.storeoperator.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2016/11/15.
 */
public class DetailsData {
    @SerializedName("id")
    private String detailsId;
    @SerializedName("name")
    private String detailsName;
    @SerializedName("qty")
    private int detailsQty;

    public String getDetailsId() {
        return detailsId;
    }

    public String getDetailsName() {
        return detailsName;
    }

    public int getDetailsQty() {
        return detailsQty;
    }
}
