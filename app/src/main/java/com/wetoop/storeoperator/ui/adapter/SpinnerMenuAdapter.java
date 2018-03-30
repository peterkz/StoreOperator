package com.wetoop.storeoperator.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.bean.MenuItem;

import java.util.List;

/**
 * Created by User on 2018/1/16.
 */

public class SpinnerMenuAdapter extends BaseAdapter {
    private List<MenuItem> menuItemArrayList;
    private Context context;

    public SpinnerMenuAdapter(Context context, List<MenuItem> menuItemArrayList) {
        this.menuItemArrayList = menuItemArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return menuItemArrayList.size();
    }

    @Override
    public Object getItem(int pos) {
        return menuItemArrayList.get(pos).toString();
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        }
        TextView menu_text = (TextView) convertView.findViewById(R.id.menu_text);
        ImageView menu_image = (ImageView) convertView.findViewById(R.id.menu_image);
        MenuItem data = menuItemArrayList.get(position);
        menu_text.setText(TextUtils.isEmpty(data.getMenuName()) ? "" : data.getMenuName());
        if (data.getIconRes() != 0) menu_image.setImageResource(data.getIconRes());
        else menu_image.setVisibility(View.GONE);
        return convertView;
    }

    // =============================================================================================
    // inner class
    // =============================================================================================
    public interface IOnItemSelectListener {
        void onItemMenuClick(int pos);
    }
}
