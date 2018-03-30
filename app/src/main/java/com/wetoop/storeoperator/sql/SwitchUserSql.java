package com.wetoop.storeoperator.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/23.
 */
public class SwitchUserSql  {
    private Context context;
    public static final String DATABASE_NAME = "StoreOperatorAdd1.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME = "switchUserSql";//表名
    public SwitchUserSql(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public boolean createTable() {
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = SwitchUserDao.getInstance(context).getWriteDataBase();
        try {
            db.execSQL("drop table if exists " + TABLENAME);
            String sql = "create table " + TABLENAME + "(id text,token text,title text,login_name text)";
            db.execSQL(sql);
            db.close();
            return true;
        }catch (Exception e){
            return false;
        }
        //return;
    }
    /**
     * 插入数据
     */
    public void insert(String sql) {
        SQLiteDatabase db = SwitchUserDao.getInstance(context).getWriteDataBase();
        //执行SQL语句
        db.execSQL(sql);
        db.close();
    }
    /**
     * 查询数据
     */
    public ArrayList<SwitchUserBean> queryDataToSQLite() {
        ArrayList<SwitchUserBean> switchUserBeans = new ArrayList<>();
        SQLiteDatabase db = SwitchUserDao.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select * from " + TABLENAME, new String[]{});
        while (cursor.moveToNext()) {
            SwitchUserBean bean = new SwitchUserBean();
            bean.setId(cursor.getString(cursor.getColumnIndex("id")));
            bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            bean.setToken(cursor.getString(cursor.getColumnIndex("token")));
            bean.setLoginName(cursor.getString(cursor.getColumnIndex("login_name")));
            //bean.setAllowPay(cursor.getString(cursor.getColumnIndex("allowPay")));
            switchUserBeans.add(bean);
        }
        return switchUserBeans;
    }
}
