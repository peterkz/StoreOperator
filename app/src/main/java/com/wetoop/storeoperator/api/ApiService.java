package com.wetoop.storeoperator.api;

import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.ResultMessage;
import com.wetoop.storeoperator.api.model.StatsUsedData;
import com.wetoop.storeoperator.api.model.UserInfo;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by bruce on 15-4-23.
 */
public interface ApiService {
    @FormUrlEncoded//发起收款
    @POST("/order-place")
    public void allow_pay(@Header("SA-Token") String token,@Field("price") String price,@Field("code") String code, Callback<ResultMessage> callback);

    @FormUrlEncoded//取消收款
    @POST("/order-status/set-cancelled")
    public void allow_cancelled_pay(@Header("SA-Token") String token,@Field("id") String id, Callback<Order> callback);

    @FormUrlEncoded
    @POST("/sign-in")
    public void signIn(@Field("u") String userName, @Field("p") String password, Callback<ResultMessage> callback);

    @FormUrlEncoded//设为已使用
    @POST("/order-status/set-used")
    public void used(@Header("SA-Token") String token,@Field("id") String used,Callback<Order> callback);

    @GET("/orders/{type}")
    public void orderList(@Header("SA-Token") String token, @Path("type") int type, Callback<List<Order>> callback);

    @GET("/orders/{type}")
    public void orderListPage(@Header("SA-Token") String token, @Path("type") int type,@Query("p") int pNum, Callback<List<Order>> callback);

    @GET("/orders/{type}")
    public void orderListSearch(@Header("SA-Token") String token, @Path("type") int type,@Query("q") String pNum, Callback<List<Order>> callback);

    @FormUrlEncoded
    @POST("/order/{id}")
    public void orderItem(@Header("SA-Token") String token, @Path("id") String orderId,@Field("alt") String alt, Callback<Order> callback);

    @GET("/stats/used/{start}|{end}")
    public void usedOrder(@Header("SA-Token") String token, @Path("start") String start,@Path("end") String end, Callback<StatsUsedData> callback);

    @GET("/user-info")
    public void checkLogin(@Header("SA-Token") String token, Callback<UserInfo> callback);
}
