package com.wetoop.storeoperator;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wetoop.storeoperator.api.ApiService;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bluetooth.v4.BH_V4Support;
import com.wetoop.storeoperator.ui.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

/**
 * Created by bruce on 15-7-2.
 */
public class App extends Application {
    public static final String PREFS_NAME = "App";

    /**
     * Contains an approximation of the battery level.
     */
    public int batteryLevel = 0;
    public static BluetoothDevice B_item;
    //运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<Activity>();
    public static boolean showLoading = false;

    private static App sApplication;
    public static UserInfo userInfo;
    public static BH_V4Support v4Support;

    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    private ApiService apiService;

    @Override
    public void onTerminate() {
        super.onTerminate();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
        MainActivity ma = new MainActivity();
        ma.stopService(null);
    }

    @Override
    public void onCreate() {
        sApplication = this;

        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                File logPath = getExternalFilesDir("crash");
                String timestamp = String.valueOf(System.currentTimeMillis());
                final StringWriter result = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(result);
                ex.printStackTrace(printWriter);
                String stacktrace = result.toString();
                printWriter.close();
                String filename = timestamp + ".log";

                if (logPath != null) {
                    try {
                        BufferedWriter bos = new BufferedWriter(new FileWriter(new File(logPath, filename)));
                        bos.write(stacktrace);
                        bos.flush();
                        bos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //Toast.makeText(App.this, "toast", Toast.LENGTH_LONG).show();
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        BooleanSerializer booleanSerializer = new BooleanSerializer();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(Boolean.class, booleanSerializer)
                .registerTypeAdapter(boolean.class, booleanSerializer)
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://wx.wetoop.com/service/store-app")
                .setConverter(new GsonConverter(gson))
                .setLogLevel(RestAdapter.LogLevel.NONE)//调试开关
                .build();
        apiService = restAdapter.create(ApiService.class);

        refreshAccount();

    }

    public static App getInstance() {
        return sApplication;
    }

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            batteryLevel = intent.getIntExtra("level", 0);
        }
    };

    //在这个函数里面，把获取的gson里面的true或者1转换成程序能识别的布尔值
    public static class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

        public JsonElement serialize(Boolean arg0, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(arg0 ? true : false);
        }

        @Override
        public Boolean deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
            return arg0.getAsBoolean();
        }
    }

    public void refreshAccount() {
        if (isSignedIn()) {

        }
    }

    public boolean isSignedIn() {
        return getToken().length() > 0;
    }

    public void signOut() {
        setToken("");
    }

    public BluetoothDevice getItem() {
        return B_item;
    }

    public void setItem(BluetoothDevice item) {
        B_item = item;
    }

    public String getPrintNum() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("printNum", "");
    }

    public void setPrintNum(String num) {//保存需要打印的张数
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("printNum", num).commit();
    }

    public String getChecked() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("checked", "");
    }

    public void setChecked(String checked) {//自动打印开启状态
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("checked", checked).commit();
    }

    public String getToken() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("token", "");
    }

    public void setToken(String token) {//登录状态
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("token", token).commit();
    }

    public String getTitle() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("title", "");
    }

    public void setTitle(String title) {//标题
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("title", title).commit();
    }

    public String getLoginName() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("loginName", "");
    }

    public void setLoginName(String loginName) {//登录名
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("loginName", loginName).commit();
    }

    public String getAllowPay() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("allowPay", "");
    }

    public void setAllowPay(String allowPay) {//收款权限
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("allowPay", allowPay).commit();
    }

    public int getPos() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("pos", 100);
    }

    public void setPos(int pos) {//记录选择的蓝牙在列表上的位置
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("pos", pos).commit();
    }

    public String getReconnection() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("Reconnection", "");
    }

    public void setReconnection(String Reconnection) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("Reconnection", Reconnection).commit();
    }

    public long getLastModified() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong("lastModified", 0);
    }

    public void setLastModified(long lastModified) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putLong("lastModified", lastModified).commit();
    }

    public long getLastModifiedService() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong("lastModifiedService", 0);
    }

    public void setLastModifiedService(long lastModifiedService) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putLong("lastModifiedService", lastModifiedService).commit();
    }

    public int getFirstUsed() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("firstUsed", 5);
    }

    public void setFirstUsed(int firstUsed) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("firstUsed", firstUsed).commit();
    }

    public String getUserId() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("userId", "");
    }

    public void setUserId(String userId) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("userId", userId).commit();
    }

    public String getTotalName() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("totalName", "");
    }

    public void setTotalName(String totalName) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("totalName", totalName).commit();
    }

    public String getShowList() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("showList", "");
    }

    public void setShowList(String showList) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("showList", showList).commit();
    }

    public int getPositon() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("tab1Position", 0);
    }

    public void setPosition(int position) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("tab1Position", position).commit();
    }

    public String getJump() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("goJump", "false");
    }

    public void setJump(String jump) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString("goJump", jump).commit();
    }

    public int getLoginTab() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("loginTab", 0);
    }

    public void setLoginTab(int loginTab) {//当已经有登录过期的操作时，不为0
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("loginTab", loginTab).commit();
    }

    public int getNFCResume() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("nfcResume", 0);
    }

    public void setNFCResume(int nfcResume) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt("nfcResume", nfcResume).commit();
    }

    public static String getErrorMessage(RetrofitError error) {
        Log.e("ERROR", error.getMessage(), error);

        String message = error.getLocalizedMessage();
        RetrofitError.Kind kind = error.getKind();
        if (kind == RetrofitError.Kind.NETWORK) {
            message = "网络错误，无法连接网络";
        } else if (kind == RetrofitError.Kind.CONVERSION) {
            message = "返回数据无效，请稍后再试";
        } else if (kind == RetrofitError.Kind.HTTP) {
            message = "服务器太忙了，请稍后再试";
        } else if (kind == RetrofitError.Kind.UNEXPECTED) {
            message = "出错";
        }

        return message;
    }

    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    //关闭每一个list内的activity
    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    //杀进程
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public ApiService getApiService() {
        return apiService;
    }
}
