package com.wetoop.storeoperator.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by User on 2018/1/18.
 */

public class StatsUsedAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Order> cameraList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StatsUsedAdapter(Context context, ArrayList<Order> cameraList) {
        this.context = context;
        this.cameraList = cameraList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return list.size();
        return cameraList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return cameraList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_order_item, null);
        }

        Order item = cameraList.get(position);
        TextView title = (TextView) view.findViewById(R.id.text_title);
        TextView price = (TextView) view.findViewById(R.id.text_price);
        TextView order_no = (TextView) view.findViewById(R.id.text_order_no);
        TextView created_date = (TextView) view.findViewById(R.id.text_created);
        TextView mobile = (TextView) view.findViewById(R.id.text_mobile);
        TextView status = (TextView) view.findViewById(R.id.status);

        if (item != null) {
            status.setPadding(0, 0, 0, 0);
            switch (item.getStatus()) {
                case CREATED:
                    status.setBackground(context.getResources().getDrawable(R.drawable.pay_wait_background));
                    status.setText("等待付款");
                    status.setTextColor(context.getResources().getColor(R.color.pay_cancel_color));
                    status.setTextSize(14);
                    status.setPadding(10, 4, 10, 4);
                    created_date.setText("下单时间：" + dateFormat.format(item.getCreatedAt()));
                    break;
                case CANCELLED:
                    status.setBackground(null);
                    status.setText("已取消");
                    status.setTextSize(16);
                    status.setTextColor(context.getResources().getColor(R.color.used_color));
                    created_date.setText("取消时间：" + dateFormat.format(item.getCancelledAt()));
                    break;
                case PAID:
                    status.setBackground(context.getResources().getDrawable(R.drawable.pay_unclicked));
                    status.setText("已付款");
                    status.setTextColor(context.getResources().getColor(R.color.pay_color));
                    status.setTextSize(14);
                    status.setPadding(10, 4, 10, 4);
                    created_date.setText("付款时间：" + dateFormat.format(item.getPurchasedAt()));
                    break;
                case REFUNDED:
                    status.setText("已退款");
                    status.setBackground(null);
                    status.setTextSize(16);
                    status.setTextColor(context.getResources().getColor(R.color.used_color));
                    created_date.setText("退款时间：" + item.getRefunded());
                    break;
                case USED:
                    status.setText("已使用");
                    status.setTextSize(16);
                    status.setBackground(null);
                    status.setTextColor(context.getResources().getColor(R.color.used_color));
                    created_date.setText("付款时间：" + dateFormat.format(item.getPurchasedAt()));
                    break;
            }
        }

        title.setText("商品名称：" + item.getTitle());
        price.setText("消费金额：" + String.format("￥%.2f", item.getTotalPrice()));
        order_no.setText(context.getString(R.string.order_no) + " " + item.getId());

        String mobileText = item.getMobile();
        if (mobileText != null) {
            mobile.setText("顾        客：" + mobileText);
        } else {
            mobile.setText("顾        客：" + "");
        }

        return view;
    }
}
