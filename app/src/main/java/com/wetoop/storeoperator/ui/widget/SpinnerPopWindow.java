package com.wetoop.storeoperator.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.ui.AllowPayActivity;
import com.wetoop.storeoperator.ui.adapter.SpinerAdapter;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.ui.adapter.SpinnerMenuAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/2.
 */

public class SpinnerPopWindow extends PopupWindow implements AdapterView.OnItemClickListener {

    private Context mContext;
    private ListView mListView;
    private SpinerAdapter titleAdapter;
    private SpinerAdapter.IOnItemSelectListener mItemSelectListener;
    private SpinnerMenuAdapter menuAdapter;
    private SpinnerMenuAdapter.IOnItemSelectListener mMenuItemSelectListener;

    public SpinnerPopWindow(Context context, int type)
    {
        super(context);
        this.mContext = context;
        if(type == 1) {//1:标题；2：菜单
            initTitle();
        }else if(type == 2){
            initMenu();
        }
    }

    public void setItemTitleListener(SpinerAdapter.IOnItemSelectListener listener){
        mItemSelectListener = listener;
    }

    public void setItemMenuListener(SpinnerMenuAdapter.IOnItemSelectListener listener){
        mMenuItemSelectListener = listener;
    }

    public void setTitleAdapter(SpinerAdapter adapter){
        titleAdapter = adapter;
        mListView.setAdapter(titleAdapter);
    }
    public void setMenuAdapter(SpinnerMenuAdapter adapter){
        menuAdapter = adapter;
        mListView.setAdapter(menuAdapter);
    }

    private void initTitle()
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_window_layout, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);

        mListView = (ListView) view.findViewById(R.id.spinner_listView);
        mListView.setOnItemClickListener(this);
        LinearLayout parent = (LinearLayout) view.findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    private void initMenu()
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_spiner_window_layout, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);

        mListView = (ListView) view.findViewById(R.id.menu_spinner_listView);
        mListView.setOnItemClickListener(this);
        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (mItemSelectListener != null){
            mItemSelectListener.onItemClick(pos);
        }
        if (mMenuItemSelectListener != null){
            mMenuItemSelectListener.onItemMenuClick(pos);
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {//解决android7.0以上版本时弹框位置不正确的问题
            int[] a = new int[2];
            anchor.getLocationInWindow(a);
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, a[1] + anchor.getHeight() + 0);
        } else {
            super.showAsDropDown(anchor);
        }
    }

}
