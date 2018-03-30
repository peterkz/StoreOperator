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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bean.OrderBean;
import com.wetoop.storeoperator.factory.PriorityExecutor;
import com.wetoop.storeoperator.sql.OrderSql;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.InputMethodTool;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.LoginActivity;
import com.wetoop.storeoperator.ui.MainActivity;
import com.wetoop.storeoperator.ui.OrderDetailActivity;
import com.wetoop.storeoperator.ui.SimpleMainActivity;
import com.wetoop.storeoperator.ui.adapter.OrderListAdapter;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 2018/1/11.
 */

public class OrderPayFragment extends Fragment {
    public static List<Order> payList = null;
    View rootView;
    private ListView listView;
    public SwipeRefreshLayout swipeRefreshLayout;
    private View mContentView;
    private ProgressBar mProgressBar;
    private TextView mHintView,noOrder;
    private EditText searchText;
    private int pageNum = 0;
    private int countTest;
    public ArrayAdapter<Order> arrayAdapter;
    public ArrayList<Order> list = new ArrayList<>(10);
    public ArrayList<Order> originalList = new ArrayList<>(10);
    private ArrayList<OrderBean> orderList = new ArrayList<>();
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private TextWatcher onQueryTextListener;
    private boolean searchBool = false;
    public static int refreshListCount = 0;
    private String Purchased_sql = "";
    private String Purchased_service = "";
    private Date created;
    private Date Purchased_created;
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
        if (OrderPayFragment.this.getActivity() != null) {
            searchText = ((OrderListFragment) OrderPayFragment.this.getParentFragment()).searchTextList;
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
                                Alerter.create(getActivity())
                                        .setText("请输入超过6位数的订单号")
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                                //Toast.makeText(getActivity(), "请输入超过6位数的订单号", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
    }

    public void adapterData() {
        arrayAdapter = new OrderListAdapter(getActivity(),list);
        listView.setAdapter(arrayAdapter);
        listView.setTextFilterEnabled(true);
        listView.dispatchDisplayHint(View.INVISIBLE);
    }

    private void setViewListener() {
        onQueryTextListener = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int countNum) {
                if(isVisibleToUser) {
                    String newText = String.valueOf(s);
                    if (!isChinese(newText) && !isConSpeCharacters(newText)) {
                        String newTextStr = newText.toLowerCase();
                        if (TextUtils.isEmpty(newTextStr)) {
                            listView.clearTextFilter();
                            if (searchBool) {
                                getData(true, 0);
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
                        Alerter.create(getActivity())
                                .setText("请输入英文或数字")
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                ImageView clearButton = ((OrderListFragment) OrderPayFragment.this.getParentFragment()).clear_button;
                if (clearButton != null) {
                    if (!TextUtils.isEmpty(s)) {
                        clearButton.setVisibility(View.VISIBLE);
                    } else {
                        clearButton.setVisibility(View.GONE);
                    }
                }
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
                Intent intent = new Intent(OrderPayFragment.this.getActivity(), OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, item);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNum = 0;
                setDateThread();
            }
        });
    }

    private void searchData(String newTextStr){
        final App app = (App) getActivity().getApplication();
        //搜索用接口，输入超过6位的订单号才能搜
        app.getApiService().orderListSearch(app.getToken(), app.getPositon(), newTextStr, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("加载失败");
                    Alerter.create(getActivity())
                            .setText("数据加载失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
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

                payList.clear();
                payList = orders;
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

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();
        if(intent.getStringExtra("loginBack")!=null){
            if("back".equals(intent.getStringExtra("loginBack"))){
                switchUserBeanArrayList.clear();
                switchUserBeanArrayList = switchUserList();
                if(switchUserBeanArrayList.size() > 0) {
                    checkOtherLogin(true);
                }
            }
        }else{
            checkOtherLogin(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null)
            getActivity().unregisterReceiver(broadcastReceiverLive);
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(getActivity());
        return cameraSql.queryDataToSQLite();
    }

    public void changListViewData() {
        App app = (App)getActivity().getApplication();
        app.setLoginTab(0);
        refreshListCount = 5;
        if (payList != null) {
            if (payList.size() > 0) {
                noOrder.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                list.clear();
                list.addAll(payList);
                originalList.clear();
                originalList.addAll(payList);
                arrayAdapter.notifyDataSetChanged();
            } else {
                String checked_n = ((App) getActivity().getApplication()).getChecked();
                if (checked_n.equals("true")) {
                    getData(true,pageNum);
                } else if (checked_n.equals("false")) {
                    getData(false,pageNum);
                }
            }
        } else {
            String checked_n = ((App) getActivity().getApplication()).getChecked();
            if (checked_n.equals("true")) {
                getData(true,pageNum);
            } else if (checked_n.equals("false")) {
                getData(false,pageNum);
            }
        }

    }

    private void checkOtherLogin(boolean pos){
        final App app = (App) getActivity().getApplication();
        String token;
        if(pos) {
            token = switchUserBeanArrayList.get(0).getToken();
        }else{
            token = app.getToken();
        }
        app.getApiService().checkLogin(token, new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 401) {
                        Toast.makeText(getActivity(),"登录过期，请重新登录",Toast.LENGTH_SHORT).show();
                        Message msg = new Message();
                        msg.what = 1;//登录过期
                        mHandler_code.sendMessage(msg);
                    }else{
                        if(getActivity()!= null)
                            changListViewData();
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

    public void getData(final boolean refresh, int page) {
        //
        final App app = (App) OrderPayFragment.this.getActivity().getApplication();
        //page为要加载第几页数据
        app.getApiService().orderListPage(app.getToken(), 0, page, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("加载失败");
                    Alerter.create(getActivity())
                            .setText("数据加载失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    return;
                }
                if(!refresh) {
                    countTest = 0;
                    int count = 0;
                    ArrayList<OrderBean> sql_list = new ArrayList<>();
                    OrderSql os = new OrderSql(getActivity());
                    for (int i = 0; i < orders.size(); i++) {
                        Order item = orders.get(i);
                        Purchased_service = item.getId();
                        Purchased_created = item.getCreatedAt();
                        sql_list.clear();
                        sql_list = os.getPurchasedId();
                        OrderBean order_B = new OrderBean();
                        order_B.setId("");
                        sql_list.add(order_B);
                        if (sql_list.size() == 0) {
                            countTest = 1;
                            break;
                        } else {
                            for (int j = 0; j < sql_list.size(); j++) {
                                Purchased_sql = sql_list.get(j).getId();
                                if (Purchased_sql != null) {
                                    if (Purchased_service.equals(Purchased_sql)) {
                                        count = 5;
                                        created = Purchased_created;
                                    }
                                }
                            }

                        }
                    }
                    if (count == 5) {
                        int j = 0;
                        for (int i = 0; i < orders.size(); i++) {
                            Order item = orders.get(i);
                            Date date = item.getCreatedAt();
                            if (date.after(created)) {
                                created = date;
                                j = i;
                            }
                        }
                        dataToSql(orders,j);
                    }
                    //用于自动打印，这里保存到数据库的是最新的订单，这东西之后会用来比较，用于确定是否需要打印
                    if (countTest == 1) {
                        dataToSql(orders,0);
                    }
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

                payList = orders;
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

    private void dataToSql(List<Order> orders,int pos){
        orderList.clear();
        Order item = orders.get(pos);
        OrderBean orderBean = new OrderBean();
        orderBean.setId(item.getId());
        orderBean.setTitle(item.getTitle());
        orderBean.setTotalPrice(item.getTotalPrice());
        orderBean.setFundsAdjust(item.getFundsAdjust());
        orderBean.setCreatedAt(item.getCreatedAt());
        orderBean.setPurchasedAt(item.getPurchasedAt());
        orderBean.setCancelledAt(item.getCancelledAt());
        orderBean.setMobile(item.getMobile());
        orderBean.setAddress(item.getAddress());
        orderBean.setUsed(item.getUsed());
        orderBean.setCustomer_note(item.getCustomer_note());
        orderBean.setRefunded(item.getRefunded());
        orderList.add(orderBean);
        if(getActivity() != null) {
            OrderSql address = new OrderSql(getActivity());
            address.createTable();
            address.setDataToSQLite(orderList);//对地址数据信息进行更新
        }
    }

    private void refreshFootList(int page) {
        final App app = (App) getActivity().getApplication();
        //获取数据
        app.getApiService().orderListPage(app.getToken(), app.getPositon(), page, new Callback<List<Order>>() {
                    @Override
                    public void success(List<Order> orders, Response response) {
                        if (orders == null) {
                            Alerter.create(getActivity())
                                    .setText("数据加载失败")
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                            return;
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        if (orders.size() > 0) {
                            list.addAll(orders);
                            originalList.addAll(orders);
                        } else {
                            pageNum = 0;
                            Alerter.create(getActivity())
                                    .setText("已无更多数据")
                                    .setBackgroundColorRes(R.color.alerter_info)
                                    .show();
                        }
                        Message msg = new Message();
                        msg.what = 4;//加载更多
                        mHandler_code.sendMessage(msg);
                        arrayAdapter.notifyDataSetChanged();
                        if (OrderPayFragment.this.getActivity() != null) {
                            EditText editText = ((OrderListFragment) OrderPayFragment.this.getParentFragment()).searchTextList;
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
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case 3:
                    App app = (App) getActivity().getApplication();
                    String checked_n = app.getChecked();
                    if (checked_n.equals("true")) {
                        getData(true,pageNum);
                    } else if (checked_n.equals("false")) {
                        getData(false,pageNum);
                    }
                    break;
                case 4:
                    mProgressBar.setVisibility(View.GONE);
                    mHintView.setText("点击查看更多");
                    break;
                case 5:
                    Alerter.create(getActivity())
                            .setText("获取数据出错")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
            }
        }
    };
    static String inputStreamToString(InputStream inputStream, String charsetName)
            throws IOException {
        final int BUFFER_SIZE = 4 * 1024;
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(inputStream, charsetName);
        char[] buffer = new char[BUFFER_SIZE];
        int length;
        while ((length = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }
    private long lastModified = 0;
    void setDateThread() {
        PriorityExecutor.getInstance().execute(new Runnable() {
            public void run() {
                InputStream is = null;
                try {
                    App app = (App) getActivity().getApplication();
                    lastModified = app.getLastModified();
                    URL url = new URL("https://wx.wetoop.com/service/store-app/user-info");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.addRequestProperty("SA-Token", app.getToken());
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    if (lastModified > 0) {
                        conn.setIfModifiedSince(lastModified);
                    }
                    conn.setDoInput(true);
                    long lastModifiedTest = conn.getLastModified();
                    // Starts the query
                    conn.connect();

                    if (conn.getResponseCode() != 304) {
                        is = conn.getInputStream();

                        try {
                            String strResult = inputStreamToString(is, "UTF-8");
                            JSONObject jsonObj = new JSONObject(strResult);
                            int codeJson = jsonObj.getInt("errorCode");
                            if (codeJson == 401&&app.getLoginTab() == 0) {
                                app.setLoginTab(1);//防止进入超过两次
                                Message msg = new Message();
                                msg.what = 1;//登录过期
                                mHandler_code.sendMessage(msg);
                            }
                        }catch (JSONException e) {
                            swipeRefreshLayout.setRefreshing(false);
                            Message msg = new Message();
                            msg.what = 5;//数据加载错误提示
                            mHandler_code.sendMessage(msg);
                        }
                    }

                    if (lastModifiedTest == 0) {
                        int code = conn.getResponseCode();
                        if (code == 304) {
                            Message msg = new Message();
                            msg.what = 2;//刷新结束
                            mHandler_code.sendMessage(msg);
                        } else if (code == 200) {
                            app.setLastModified(lastModifiedTest);
                            Message msg = new Message();
                            msg.what = 3;//重新加载
                            mHandler_code.sendMessage(msg);
                        }
                    } else {
                        app.setLastModified(lastModifiedTest);
                        Message msg = new Message();
                        msg.what = 3;//重新加载
                        mHandler_code.sendMessage(msg);
                    }

                } catch (IOException e) {
                    swipeRefreshLayout.setRefreshing(false);
                    Message msg = new Message();
                    msg.what = 5;//数据加载失败提示
                    mHandler_code.sendMessage(msg);
                }  finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        });
    }
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
            if (intent.getExtras().getString("jump") != null) {
                String jump = intent.getExtras().getString("jump");
                if (jump != null) {
                    if (jump.equals("simple")) {
                        Intent intent1 = new Intent(getActivity(), SimpleMainActivity.class);
                        startActivity(intent1);
                        getActivity().finish();
                    }
                }
            }
        }
    }
}
