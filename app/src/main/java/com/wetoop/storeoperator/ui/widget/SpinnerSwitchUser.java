package com.wetoop.storeoperator.ui.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.ui.adapter.SpinnerSwitchAdapter;

/**
 * Created by User on 2018/3/16.
 */

public class SpinnerSwitchUser extends PopupWindow implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ListView mListView;
    private SpinnerSwitchAdapter switchAdapter;
    private SpinnerSwitchAdapter.IOnItemSwitchSelectListener mSwitchAdapter;

    public SpinnerSwitchUser(Context context){
        super(context);
        this.mContext = context;
        init();
    }

    public void setItemSwitchListener(SpinnerSwitchAdapter.IOnItemSwitchSelectListener listener){
        mSwitchAdapter = listener;
    }

    public void setSwitchAdapter(SpinnerSwitchAdapter adapter){
        switchAdapter = adapter;
        mListView.setAdapter(switchAdapter);
    }

    private void init(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_switch, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);

        mListView = (ListView) view.findViewById(R.id.spinner_switch_listView);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        if (mSwitchAdapter != null){
            mSwitchAdapter.onItemSwitchClick(position);
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
