package com.wetoop.storeoperator.tools;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.ui.LoginActivity;

/**
 * Created by User on 2018/2/2.
 */

public class LoginOutTools {
    public static void deleteUser(Activity activity,String id){
        SwitchUserSql switchUserSql = new SwitchUserSql(activity);
        switchUserSql.insert("delete from switchUserSql where id='" + id + "'");//删除用户
    }
    public static void goToLogin(App app,Activity activity){
        app.setLoginTab(1);
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        app.setChecked("false");
        activity.finish();
    }
    /**切换其他账号，但不删除当前账号*/
    public static void loginSimple(App app,Activity activity){
        app.setLoginTab(1);
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("outLogin",app.getShowList());
        intent.putExtra("switchUser","change");
        activity.startActivity(intent);
        activity.finish();
    }
    public static void signOutSimple(App app,Activity activity){
        app.setLoginTab(1);
        SwitchUserSql switchUserSql = new SwitchUserSql(activity);
        switchUserSql.insert("delete from switchUserSql where id='" + app.getUserId() + "'");
        Intent intent = new Intent(activity, LoginActivity.class);
        app.setChecked("false");
        intent.putExtra("outLogin",app.getShowList());
        intent.putExtra("exit","come");
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
        }
        activity.startActivity(intent);
        activity.finish();
    }
    public static void loginPastDue(App app,Activity activity){
        app.setLoginTab(1);
        SwitchUserSql switchUserSql = new SwitchUserSql(activity);
        switchUserSql.insert("delete from switchUserSql where id='" + app.getUserId() + "'");
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("userName",app.getLoginName());
        intent.putExtra("outLogin", app.getShowList());//设标记，可用于返回当前界面
        activity.startActivity(intent);
        activity.finish();
        app.exit();
    }
    public static void loginPastDue(App app,Activity activity,String userId,String loginName){
        app.setLoginTab(1);
        SwitchUserSql switchUserSql = new SwitchUserSql(activity);
        switchUserSql.insert("delete from switchUserSql where id='" + userId + "'");
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("userName",loginName);
        intent.putExtra("outLogin", app.getShowList());//设标记，可用于返回当前界面
        activity.startActivity(intent);
        activity.finish();
    }
    public static void loginPastDueFinish(App app,Activity activity){
        loginPastDueFinish(app,activity,true);
    }
    public static void loginPastDueFinish(App app,Activity activity,boolean finish){
        app.setLoginTab(1);
        app.setChecked("false");
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
        }
        SwitchUserSql switchUserSql = new SwitchUserSql(activity);
        switchUserSql.insert("delete from switchUserSql where id='" + app.getUserId() + "'");
        Intent intent = new Intent(activity, LoginActivity.class);
        if (!finish)
            intent.putExtra("allow_finish", "true");
        intent.putExtra("finish", "true");
        intent.putExtra("userName",app.getLoginName());
        intent.putExtra("outLogin", app.getShowList());//设标记，可用于返回当前界面
        activity.startActivity(intent);
        if(finish) {
            activity.finish();
            app.exit();
        }
    }
}
