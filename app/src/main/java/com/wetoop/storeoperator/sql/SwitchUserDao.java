package com.wetoop.storeoperator.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/3/23.
 */
public class SwitchUserDao  {
    private static SwitchUserDao instance = null;
    private DatabaseHelper mHelper;
    private SQLiteDatabase sqLiteDatabase;
    public static final String DATABASE_NAME = "StoreOperatorAdd1.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME = "switchUserSql";//表名

    private SwitchUserDao(Context context) {
        mHelper = new DatabaseHelper(context);
        sqLiteDatabase = mHelper.getWritableDatabase();
    }

    /**
     * 单例模式创建数据库
     *
     * @param context
     * @return
     */
    public static SwitchUserDao getInstance(Context context) {
        if (instance == null) {
            instance = new SwitchUserDao(context);
        }
        return instance;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);//数据库名称Storeoperator.db，版本为1
        }

        //当数据库第一次生成时调用这个方法
        @Override
        public void onCreate(SQLiteDatabase db) {
            //id，区域名，详细地址，上级区域
            String orderSql = "create table " + TABLENAME + "(id text,token text,title text,login_name text)";
            db.execSQL(orderSql);
        }

        //数据库更新时调用这个方法
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

    /**
     * 获取可写数据库
     */

    public SQLiteDatabase getWriteDataBase() {
        return mHelper.getWritableDatabase();
    }

    /**
     * 获取可读数据库
     */
    public SQLiteDatabase getReadDatabase() {
        return mHelper.getReadableDatabase();
    }
}