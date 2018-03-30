package com.wetoop.storeoperator.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Administrator on 2015/9/25.
 */
public class AutoPrintUtil {
    public static BluetoothDevice item = null;
    //开启轮询服务
    public static void startPollingService(Context context, int seconds, Class<?> cls,BluetoothDevice B_item) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        item=B_item;

        //包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        //intent.setAction(action);
        intent.putExtra("orderItem", B_item);
        intent.setPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                seconds * 1000, pendingIntent);
    }

    /*//再次开启服务
    public static void startAgainPollingService(Context context, int seconds, Class<?> cls,String action) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        //包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        //intent.setAction(action);
        intent.putExtra("orderItem", "again");
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                seconds * 1000, pendingIntent);
    }*/

    //停止轮询服务
    public static void stopPollingService(Context context, Class<?> cls,BluetoothDevice B_item) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        //intent.setAction(action);
        intent.putExtra("orderItem", B_item);
        intent.setPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }
}
