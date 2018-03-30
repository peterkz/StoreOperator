package com.wetoop.storeoperator.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.wetoop.storeoperator.ui.fragment.OrderCancelFragment.cancelList;
import static com.wetoop.storeoperator.ui.fragment.OrderPayFragment.payList;
import static com.wetoop.storeoperator.ui.fragment.OrderWaitFragment.waitList;

/**
 * Created by User on 2018/1/11.
 */

public class OrderListFragment extends Fragment{
    private static final String PAY = "PAY";
    private static final String WAIT = "WAIT";
    private static final String CANCEL = "CANCEL";

    private TabHost mTabHost;
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragmentList;
    private View rootView;
    private RelativeLayout searchVis,searchR,search;
    private TextView cancel_button;
    public ImageView clear_button;
    public EditText searchTextList;
    public TextView textSearch;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_list, container, false);
        Intent intent = getActivity().getIntent();
        int clearList = intent.getIntExtra("clearList", 0);
        if (clearList == 1) {
            if (payList != null)
                payList.clear();
            if (waitList != null)
                waitList.clear();
            if (cancelList != null)
                cancelList.clear();
        }
        initView();
        initializeTabs();
        inVariable();
        mTabHost.setOnTabChangedListener(listener);
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), false);
        searchInit();
        return rootView;
    }
    private void initView(){
        mTabHost = (TabHost)rootView.findViewById(R.id.tabHost);
        mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
        mTabHost.setup();
        //mTabHost.setOnTabChangedListener(listener);
        searchVis = (RelativeLayout) rootView.findViewById(R.id.searchVis);
        searchR = (RelativeLayout) rootView.findViewById(R.id.searchR);
        search = (RelativeLayout) rootView.findViewById(R.id.search);
        clear_button = (ImageView) rootView.findViewById(R.id.clear_button);
        searchTextList = (EditText) rootView.findViewById(R.id.search_text);
        cancel_button = (TextView) rootView.findViewById(R.id.cancel_button);
        textSearch = (TextView) rootView.findViewById(R.id.textSearch);
    }
    private void searchInit(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchVis.setVisibility(View.VISIBLE);
                searchR.setVisibility(View.GONE);
                searchTextList.requestFocus();
                InputMethodManager imm = (InputMethodManager) searchTextList.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchTextList, InputMethodManager.SHOW_FORCED);
            }
        });
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchVis.setVisibility(View.GONE);
                searchR.setVisibility(View.VISIBLE);
                searchTextList.clearFocus();
                searchTextList.setText("");
                InputMethodManager imm = (InputMethodManager) searchTextList.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(searchTextList.getApplicationWindowToken(), 0);
                }
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextList.setText("");
            }
        });
    }
    private void initializeTabs() {
        TabHost.TabSpec spec;
        //已付款
        spec = mTabHost.newTabSpec(PAY);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                View viewById = rootView.findViewById(R.id.tab_content_frame);
                return viewById;
            }
        });
        spec.setIndicator(createTabView(getString(R.string.status_purchased)));
        mTabHost.addTab(spec);
        //等待付款
        spec = mTabHost.newTabSpec(WAIT);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return rootView.findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(getString(R.string.status_created)));
        mTabHost.addTab(spec);
        //取消的订单
        spec = mTabHost.newTabSpec(CANCEL);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return rootView.findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(getString(R.string.status_cancelled)));
        mTabHost.addTab(spec);
    }
    private void inVariable() {
        fragmentList = new ArrayList<>();
        Fragment orderPayFragment = new OrderPayFragment();
        Fragment orderWaitFragment = new OrderWaitFragment();
        Fragment orderCancelFragment = new OrderCancelFragment();
        fragmentList.add(orderPayFragment);
        fragmentList.add(orderWaitFragment);
        fragmentList.add(orderCancelFragment);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new MyPageAdapter(getChildFragmentManager()));
    }
    private View createTabView(final String text) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.tabs_bar_item_order, null);
        TextView textView = (TextView) view.findViewById(R.id.tab_text_records);
        textView.setText(text);
        return view;
    }
    private void setTabBarItemIndicator(View view, boolean active) {
        TextView textView = (TextView) view.findViewById(R.id.tab_text_records);
        TextView view1 = (TextView) view.findViewById(R.id.view_records);
        if (active) {
            textView.setTextColor(getResources().getColor(R.color.order_blue));
            view1.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.order_black));
            view1.setVisibility(View.GONE);
        }
    }
    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (PAY.equals(tabId)) {
                mViewPager.setCurrentItem(0);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (WAIT.equals(tabId)) {
                mViewPager.setCurrentItem(1);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (CANCEL.equals(tabId)) {
                mViewPager.setCurrentItem(2);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), true);
            }
        }

    };
    private class MyPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
    private class MyPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        /**
         * 滑动ViewPager的时候,让上方的HorizontalScrollView自动切换
         */
        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub
            App app = (App)getActivity().getApplication();
            if(app.getPositon() != position) {
                searchTextList.setText("");
                app.setPosition(position);
            }
            if (position == 0) {
                textSearch.setText("搜索已付款订单");
                searchTextList.setHint("搜索已付款订单");
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (position == 1) {
                textSearch.setText("搜索等待付款订单");
                searchTextList.setHint("搜索等待付款订单");
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (position == 2) {
                textSearch.setText("搜索已取消订单");
                searchTextList.setHint("搜索已取消订单");
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(2), true);
            }
        }
    }

}
