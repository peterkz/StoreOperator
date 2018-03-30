package com.wetoop.storeoperator.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wetoop.storeoperator.R;

import java.util.List;

/**
 * Created by User on 2018/3/16.
 */

public class SpinnerSwitchAdapter extends BaseAdapter {
    private List<String> itemArrayList;
    private Context context;

    public SpinnerSwitchAdapter(Context context, List<String> itemArrayList) {
        this.itemArrayList = itemArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return itemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_switch_item, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text);
        textView.setText(itemArrayList.get(position));
        return convertView;
    }

    public interface IOnItemSwitchSelectListener {
        void onItemSwitchClick(int pos);
    }
}
