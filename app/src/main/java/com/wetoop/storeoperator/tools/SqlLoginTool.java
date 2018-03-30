package com.wetoop.storeoperator.tools;

import android.app.Activity;

import com.wetoop.storeoperator.sql.SwitchUserSql;

/**
 * Created by User on 2018/3/14.
 */

public class SqlLoginTool {
    public static void insertSql(Activity activity,String id,String result,String title_name,String username){
        SwitchUserSql address = new SwitchUserSql(activity);
        String sql = "insert into switchUserSql values ('" + id + "','" + result + "','" + title_name + "','" + username + "')";
        address.insert(sql);
    }
    public static void updateSql(Activity activity,String id,String result,String username){
        SwitchUserSql address = new SwitchUserSql(activity);
        String sql = "UPDATE switchUserSql SET token = '" + result + "',login_name = '" + username + "' WHERE id='" + id + "'";
        address.insert(sql);
    }
}
