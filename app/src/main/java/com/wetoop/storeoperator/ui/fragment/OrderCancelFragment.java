package com.wetoop.storeoperator.ui.fragment;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.tools.InputMethodTool;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.OrderDetailActivity;
import com.wetoop.storeoperator.ui.adapter.OrderListAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 2018/1/11.
 */

public class OrderCancelFragment extends Fragment {
    public static List<Order> cancelList = null;
    private View rootView;
    private ListView listView;
    public SwipeRefreshLayout swipeRefreshLayout;
    private View mContentView;
    private ProgressBar mProgressBar;
    private TextView mHintView,noOrder;
    private EditText searchText;
    private int pageNum = 0;
    public ArrayAdapter<Order> arrayAdapter;
    public ArrayList<Order> list = new ArrayList<>(10);
    public ArrayList<Order> originalList = new ArrayList<>(10);
    private TextWatcher onQueryTextListener;
    private boolean searchBool = false;
    public static int refreshListCount = 0;
    private boolean isVisibleToUser;
    private MyBroadcastReceiver broadcastReceiverLive;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_listview, container, false);
        initView();
        setViewListener();
        setDataView();
        return rootView;
    }
    private void initView(){
        listView = (ListView) rootView.findViewById(R.id.listView);
        listView.addFooterView(LayoutInflater.from(getActivity()).inflate(
                R.layout.listview_footer, null));
        adapterData();
        mContentView = rootView.findViewById(R.id.xlistview_footer_content);
        mHintView = (TextView) rootView.findViewById(R.id.xlistview_footer_hint_textview);
        noOrder = (TextView) rootView.findViewById(R.id.noOrder);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.xlistview_footer_progressbar);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
    }
    private void setDataView(){
        mContentView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mHintView.setText("点击查看更多");
        broadcastReceiverLive = new MyBroadcastReceiver();
        IntentFilter intentFilterLive = new IntentFilter("callRefresh");
        getActivity().registerReceiver(broadcastReceiverLive, intentFilterLive);
        if (OrderCancelFragment.this.getActivity() != null) {
            searchText = ((OrderListFragment) OrderCancelFragment.this.getParentFragment()).searchTextList;
            if (searchText != null) {
                searchText.addTextChangedListener(onQueryTextListener);
                searchText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId == EditorInfo.IME_ACTION_SEARCH){
                            if (v.getText().length() > 6) {
                                InputMethodTool.cancelInput(searchText);
                                searchData(v.getText().toString());
                            }else{
                                Toast.makeText(getActivity(), "请输入超过6位数的订单号", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        changListViewData();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null)
            getActivity().unregisterReceiver(broadcastReceiverLive);
    }

    public void changListViewData() {
        App app = (App)getActivity().getApplication();
        app.setLoginTab(0);
        refreshListCount = 5;
        if (cancelList != null) {
            if (cancelList.size() > 0) {
                noOrder.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                list.addAll(cancelList);
                originalList.clear();
                originalList.addAll(cancelList);
                arrayAdapter.notifyDataSetChanged();
            } else {
                if(OrderCancelFragment.this.getActivity() != null)
                    getData(pageNum);
            }
        } else {
            if(OrderCancelFragment.this.getActivity() != null)
                getData(pageNum);
        }

    }

    private void checkOtherLogin(){
        final App app = (App) getActivity().getApplication();
        app.getApiService().checkLogin(app.getToken(), new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 401) {
                        Toast.makeText(getActivity(),"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
                        Message msg = new Message();
                        msg.what = 1;//登录过期
                        mHandler_code.sendMessage(msg);
                    }else{
                        getData(0);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                noOrder.setVisibility(View.VISIBLE);
                noOrder.setText("加载失败");
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                originalList.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    public void getData(int page) {
        final App app = (App) OrderCancelFragment.this.getActivity().getApplication();
        //page为要加载第几页数据
        app.getApiService().orderListPage(app.getToken(), 2, page, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("加载失败");
                    Toast.makeText(OrderCancelFragment.this.getActivity(), "数据加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(orders.size() > 0){
                    noOrder.setVisibility(View.GONE);
                }else{
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("没有数据");
                }
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                list.addAll(orders);

                originalList.clear();
                originalList.addAll(orders);

                cancelList = orders;
                listView.setVisibility(View.VISIBLE);
                mContentView.setVisibility(View.VISIBLE);

                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void failure(RetrofitError error) {
                noOrder.setVisibility(View.VISIBLE);
                noOrder.setText("加载失败");
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                originalList.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setViewListener() {
        onQueryTextListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int countType) {
                if(isVisibleToUser) {
                    String newText = String.valueOf(s);
                    if (!isChinese(newText) && !isConSpeCharacters(newText)) {
                        String newTextStr = newText.toLowerCase();
                        if (TextUtils.isEmpty(newTextStr)) {
                            listView.clearTextFilter();
                            if (searchBool) {
                                getData(0);
                                searchBool = false;
                            }
                        } else {
                            mContentView.setVisibility(View.GONE);
                            if (newTextStr.length() > 6) {
                                searchData(newTextStr);
                            }
                            listView.setFilterText(newTextStr);
                            listView.dispatchDisplayHint(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(OrderCancelFragment.this.getActivity(), "请输入英文或数字", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        };

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mHintView.setText("正在加载···");
                pageNum++;
                refreshFootList(pageNum);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order item = list.get(position);
                Intent intent = new Intent(OrderCancelFragment.this.getActivity(), OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, item);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNum = 0;
                checkOtherLogin();
            }
        });
    }

    private void searchData(String newTextStr){
        final App app = (App) getActivity().getApplication();
        //获取数据
        app.getApiService().orderListSearch(app.getToken(), app.getPositon(), newTextStr, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("加载失败");
                    Toast.makeText(OrderCancelFragment.this.getActivity(), "数据加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(orders.size() > 0){
                    noOrder.setVisibility(View.GONE);
                }else{
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("没有数据");
                }
                searchBool = true;
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                list.addAll(orders);

                originalList.clear();
                originalList.addAll(orders);

                cancelList.clear();
                cancelList = orders;
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                noOrder.setVisibility(View.VISIBLE);
                noOrder.setText("加载失败");
                searchBool = true;
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                originalList.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    public void adapterData() {
        arrayAdapter = new OrderListAdapter(getActivity(),list);
        listView.setAdapter(arrayAdapter);
        listView.setTextFilterEnabled(true);
        listView.dispatchDisplayHint(View.INVISIBLE);
    }

    private void refreshFootList(int page) {
        final App app = (App) getActivity().getApplication();
        //获取数据
        app.getApiService().orderListPage(app.getToken(), 2, page, new Callback<List<Order>>() {
                    @Override
                    public void success(List<Order> orders, Response response) {
                        if (orders == null) {
                            Toast.makeText(OrderCancelFragment.this.getActivity(), "数据加载失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        if (orders.size() > 0) {
                            list.addAll(orders);
                            originalList.addAll(orders);
                        } else {
                            pageNum = 0;
                            Toast.makeText(App.getInstance(), "已无更多数据", Toast.LENGTH_LONG).show();
                        }
                        Message msg = new Message();
                        msg.what = 2;//加载更多
                        mHandler_code.sendMessage(msg);
                        arrayAdapter.notifyDataSetChanged();
                        if (OrderCancelFragment.this.getActivity() != null) {
                            EditText editText = ((OrderListFragment) OrderCancelFragment.this.getParentFragment()).searchTextList;
                            if (editText != null) {
                                editText.addTextChangedListener(onQueryTextListener);
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        swipeRefreshLayout.setRefreshing(false);
                        list.clear();
                        originalList.clear();
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
        );
    }

    public static boolean isConSpeCharacters(String string) {
        if (string.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*\\s*", "").length() == 0) {
            //不包含特殊字符 
            return false;
        }
        return true;
    }

    // 判断一个字符串是否含有中文
    public boolean isChinese(String str) {
        if (str == null)
            return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c))
                return true;// 有一个中文字符就返回
        }
        return false;
    }

    public boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    Handler mHandler_code = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    swipeRefreshLayout.setRefreshing(false);
                    list.clear();
                    originalList.clear();
                    arrayAdapter.notifyDataSetChanged();
                    if (getActivity() != null) {
                        App app = (App) getActivity().getApplication();
                        LoginOutTools.loginPastDue(app,getActivity(),app.getUserId(),app.getLoginName());
                        app.setChecked("false");
                        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
                    }
                    break;
                case 2:
                    mProgressBar.setVisibility(View.GONE);
                    mHintView.setText("点击查看更多");
                    break;
            }
        }
    };
    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("refresh") != null) {
                String refresh = intent.getExtras().getString("refresh");
                if (refresh != null) {
                    if (refresh.equals("true")) {
                        pageNum = 0;//页数归零
                        changListViewData();
                    }
                }
            }
        }
    }
}
