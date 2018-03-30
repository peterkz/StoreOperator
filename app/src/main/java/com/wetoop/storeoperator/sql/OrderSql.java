package com.wetoop.storeoperator.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wetoop.storeoperator.bean.OrderBean;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/9/14.
 */
public class OrderSql {
    private Context context;
    public static final String DATABASE_NAME = "StoreOperatorAdd2.db";//数据库名称
    public static final int DATABASE_VERSION = 1;//数据库版本
    public static final String TABLENAME = "orderTableAdd";//表名
    public OrderSql(Context context){
        this.context = context;
    }


    /**
     * 重新建立数据表
     */
    public void createTable() {
        if(context == null)
            return;
        //打开数据库（以可写的方式打开）
        SQLiteDatabase db = Dao.getInstance(context).getWriteDataBase();

        db.execSQL("drop table if exists " + TABLENAME);
        String sql = "create table " + TABLENAME + "(id text,title text,totalPrice text,fundsAdjust text,createdAt text,purchasedAt text,cancelledAt text,mobile text,address text,used text,customer_note text,refunded text,bonus_price text)";
        db.execSQL(sql);
        db.close();

        //return;
    }

    /**
     * 初始化时，将所有地址的数据填入数据表中
     */
    public void setDataToSQLite(ArrayList<OrderBean> list) {
        SQLiteDatabase db = Dao.getInstance(context).getWriteDataBase();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                    db.execSQL("insert into " + TABLENAME + " values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            new Object[]{list.get(i).getId(), list.get(i).getTitle(),list.get(i).getTotalPrice(), list.get(i).getFundsAdjust(),list.get(i).getCreatedAt(),list.get(i).getPurchasedAt(), list.get(i).getCancelledAt(), list.get(i).getMobile(),list.get(i).getAddress(),list.get(i).getUsed(),list.get(i).getCustomer_note(),list.get(i).getRefunded(),list.get(i).getBonus_price()});
            }
        }
        db.close();
    }

    public ArrayList<OrderBean> getPurchasedId(){
        ArrayList<OrderBean> list = new ArrayList<OrderBean>();
        SQLiteDatabase db = Dao.getInstance(context).getWriteDataBase();
        Cursor cursor = db.rawQuery("select id from " + TABLENAME, new String[]{});
        while (cursor.moveToNext()){
            OrderBean address = new OrderBean();
            address.setId(cursor.getString(cursor.getColumnIndex("id")));
            list.add(address);
        }
        cursor.close();
        db.close();
        return list;
    }

}
