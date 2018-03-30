package com.wetoop.storeoperator.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2017/4/25.
 */

public class PagerTabPagerAdapter extends PagerAdapter {

    List<View> views;

    public PagerTabPagerAdapter(List<View> views) {
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container, position, object);

        container.removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // return super.instantiateItem(container, position);
        View view= views.get(position);
        container.addView(view);

        return view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title="";
        if(position==0){
            title="已付款";
        }else if(position==1){
            title="等待付款";
        }else if(position==2){
            title="已退款";
        }
        return title;
    }
}
