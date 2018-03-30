package com.wetoop.storeoperator.sql;

/**
 * Created by Administrator on 2017/3/23.
 */
public class SwitchUserBean {
    private String id;
    private String title;
    private String token;
    private String allowPay;
    private String loginName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAllowPay() {
        return allowPay;
    }

    public void setAllowPay(String allowPay) {
        this.allowPay = allowPay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
