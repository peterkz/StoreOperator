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
 * Created by Administrator on 2017/5/2.
 */

public class SpinerAdapter extends BaseAdapter {

    public static interface IOnItemSelectListener{
        public void onItemClick(int pos);
    };

    private ArrayList<SwitchUserBean> switchUserBeansList;
    private Context context;
    private LayoutInflater mInflater;

    public SpinerAdapter(Context context, ArrayList<SwitchUserBean> switchUserBeansList){
        this.context = context;
        this.switchUserBeansList = switchUserBeansList;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(ArrayList<SwitchUserBean> msWitchUserBeansList, int selIndex){
        switchUserBeansList = msWitchUserBeansList;
        if (selIndex < 0){
            selIndex = 0;
        }
        if (selIndex >= switchUserBeansList.size()){
            selIndex = switchUserBeansList.size() - 1;
        }
    }

    @Override
    public int getCount() {

        return switchUserBeansList.size();
    }

    @Override
    public Object getItem(int pos) {
        return switchUserBeansList.get(pos).toString();
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.switch_user_item, null);
        }
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView titleName = (TextView) convertView.findViewById(R.id.title_name);
        ImageView myImage = (ImageView)convertView.findViewById(R.id.addImage);
        if (switchUserBeansList.size() > 0) {
            if (switchUserBeansList.get(position).getTitle().indexOf("-") > 0) {
                String[] s1 = switchUserBeansList.get(position).getTitle().split("-");
                title.setText(s1[0]);
                titleName.setText(s1[1]);
            } else {
                title.setText(switchUserBeansList.get(position).getTitle());
            }
            if (switchUserBeansList.size() == (position + 1)) {
                myImage.setVisibility(View.VISIBLE);
                titleName.setVisibility(View.GONE);
            } else {
                myImage.setVisibility(View.GONE);
                titleName.setVisibility(View.VISIBLE);
            }
            App app = (App) context.getApplicationContext();
            if (switchUserBeansList.get(position).getToken().equals(app.getToken())) {
                title.setTextColor(convertView.getResources().getColor(R.color.title_color));
            } else {
                title.setTextColor(convertView.getResources().getColor(R.color.action_bar_color_text));
            }
        }else{
            myImage.setVisibility(View.VISIBLE);
        }
        return convertView;
    }


   /* public static class ViewHolder
    {
        public TextView mTextView;
    }*/

}
