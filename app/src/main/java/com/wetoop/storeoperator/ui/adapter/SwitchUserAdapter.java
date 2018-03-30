package com.wetoop.storeoperator.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.sql.SwitchUserBean;

import java.util.ArrayList;

/**
 * Created by User on 2018/1/17.
 */

public class SwitchUserAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SwitchUserBean> switchUserBeansList;

    public SwitchUserAdapter(Context context, ArrayList<SwitchUserBean> switchUserBeansList) {
        this.context = context;
        this.switchUserBeansList = switchUserBeansList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return list.size();
        return switchUserBeansList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return switchUserBeansList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.switch_user_item_dialog, null);
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView userName = (TextView) convertView.findViewById(R.id.userName);
        ImageView myImage = (ImageView) convertView.findViewById(R.id.chooseImage);
        if (switchUserBeansList.size() > 0) {
            if (switchUserBeansList.get(position).getTitle().indexOf("-") > 0) {
                String[] s1 = switchUserBeansList.get(position).getTitle().split("-");
                title.setText(s1[0]);
                userName.setText(s1[1]);
            } else {
                title.setText(switchUserBeansList.get(position).getTitle());
                userName.setText("无");
            }
        }
        App app = (App) context.getApplicationContext();
        if (switchUserBeansList.get(position).getToken().equals(app.getToken())) {
            myImage.setVisibility(View.VISIBLE);
        } else {
            myImage.setVisibility(View.GONE);
        }

        return convertView;
    }
}