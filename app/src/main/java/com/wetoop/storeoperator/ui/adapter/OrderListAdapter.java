package com.wetoop.storeoperator.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Parck.
 * @date 2018/1/15.
 * @desc
 */

public class OrderListAdapter extends ArrayAdapter<Order> {

    private Context context;
    private List<Order> datas = new ArrayList<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public OrderListAdapter(@NonNull Context context, List<Order> datas) {
        super(context, R.layout.list_order_item, R.id.text_title, datas);
        this.context = context;
        this.datas = datas;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Order item = getItem(position);
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
                    status.setTextColor(context.getResources().getColor(R.color.pay_wait_color));
                    status.setTextSize(14);
                    status.setPadding(10, 4, 10, 4);
                    created_date.setText("下单时间：" + dateFormat.format(item.getCreatedAt()));
                    break;
                case CANCELLED:
                    status.setBackground(null);
                    status.setText("已取消");
                    status.setTextSize(16);
                    status.setTextColor(context.getResources().getColor(R.color.pay_cancel_color));
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
                    created_date.setText(getContext().getString(R.string.purchased)+ dateFormat.format(item.getPurchasedAt()));
                    break;
            }
        }

        title.setText("商品名称：" + item.getTitle());
        price.setText("消费金额：" + String.format("￥%.2f", item.getTotalPrice()));
        order_no.setText(item.getId());

        String mobileText = item.getMobile();
        if (mobileText != null) {
            mobile.setText("顾        客：" + mobileText);
        } else {
            mobile.setText("顾        客：" + "");
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        private ArrayList<Order> suggestions = new ArrayList<>(10);

        @Override
        public String convertResultToString(Object resultValue) {
            return ((Order) (resultValue)).getId();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null && constraint.length() > 0) {
                suggestions.clear();
                for (Order item : datas) {
                    String name = item.getId();
                    String mobile = item.getMobile();

                    if (name.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            mobile.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(item);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                FilterResults filterResults = new FilterResults();
                filterResults.values = new ArrayList<>(datas);
                filterResults.count = datas.size();
                return filterResults;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Order> filteredList = (ArrayList<Order>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Order c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}