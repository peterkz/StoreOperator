package com.wetoop.storeoperator.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.BluetoothService;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.ScheduledExecutorService;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bean.MenuItem;
import com.wetoop.storeoperator.bean.OrderBean;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;
import com.wetoop.storeoperator.bottombar.BottomBar;
import com.wetoop.storeoperator.bottombar.OnTabReselectListener;
import com.wetoop.storeoperator.bottombar.OnTabSelectListener;
import com.wetoop.storeoperator.sql.OrderSql;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.InputMethodTool;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.adapter.BluetoothPrinterAdapter;
import com.wetoop.storeoperator.ui.adapter.SpinerAdapter;
import com.wetoop.storeoperator.ui.adapter.SpinnerMenuAdapter;
import com.wetoop.storeoperator.ui.dialog.HintDialog;
import com.wetoop.storeoperator.ui.dialog.LoadingDialog;
import com.wetoop.storeoperator.ui.dialog.SplashDialog;
import com.wetoop.storeoperator.ui.fragment.OrderListFragment;
import com.wetoop.storeoperator.ui.fragment.UserFragment;
import com.wetoop.storeoperator.ui.fragment.VerifyFragment;
import com.wetoop.storeoperator.ui.widget.MyViewPager;
import com.wetoop.storeoperator.ui.widget.SpinnerPopWindow;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;
import static com.wetoop.storeoperator.ui.fragment.OrderCancelFragment.cancelList;
import static com.wetoop.storeoperator.ui.fragment.OrderPayFragment.payList;
import static com.wetoop.storeoperator.ui.fragment.OrderWaitFragment.waitList;

public class MainActivity extends AppCompatActivity implements SpinerAdapter.IOnItemSelectListener, SpinnerMenuAdapter.IOnItemSelectListener {
    private static final String CURRENT_FRAGMENT = "STATE_FRAGMENT_SHOW";
    private BluetoothHandler bluetoothHandler;
    private BluetoothPrinterAdapter adapter;
    public static BluetoothDevice B_item = null;
    private MyViewPager mViewPager;
    private ArrayList<Fragment> fragmentList;
    private TextView title_text, title_textTab3;
    private ImageView userImage;
    private RelativeLayout menu, exit, titleR, vouch;
    public TextView menuBackground;
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private List<MenuItem> menuItems = new ArrayList<>();
    private SpinnerPopWindow mSpinnerPopWindow, menuSpinner;
    private SpinnerMenuAdapter mAdapter;
    private MyBroadcastReceiver broadcastReceiverLive;
    private SplashDialog splashDialog;
    private OrderListFragment orderListFragment;
    private String exitCome = "no";//用于标记是否要显示选择账号列表
    private String loginTimeOutCheck = "no";//用于标记是否来自登陆过期的返回
    private LoadToast loadToast;
    private HintDialog logoutDialog;

    private Handler handler = new Handler();
    private Runnable hideTask = new Runnable() {
        @Override
        public void run() {
            splashDialog.hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.splashDialog = new SplashDialog(this);
        setWindowStatusBarColor(MainActivity.this, R.color.title_clicked_color);
        App app = (App) getApplication();
        if ("true".equals(app.getChecked())) app.setChecked("false");
        setContentView(R.layout.activity_main);
        app.addActivity(this);
        app.setPosition(0);
        initView();
        inVariable();
        popMenuList();
        //popWindowUserList();
        broadcastReceiverLive = new MyBroadcastReceiver();
        IntentFilter intentFilterLive = new IntentFilter("callRefresh");
        registerReceiver(broadcastReceiverLive, intentFilterLive);
        Intent intent = getIntent();
        if (intent.getStringExtra("loginBack") != null) {
            loginTimeOutCheck = intent.getStringExtra("loginBack");
            if ("back".equals(intent.getStringExtra("loginBack"))) {
                ArrayList<SwitchUserBean> switchUserLoginBackList = switchUserList();
                if (switchUserLoginBackList.size() > 0) {
                    checkOtherLogin(0, true);
                }
            }
        }
        if (intent.getStringExtra("exitCome") != null)
            exitCome = intent.getStringExtra("exitCome");
        if (app.getTitle() != null) {
            title_text.setText(app.getTitle());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App app = (App) getApplication();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (app.isSignedIn()) {
            if (App.showLoading) {
                App.showLoading = false;
                splashDialog.show();
                handler.postDelayed(hideTask, 1500);
            }
        } else {
            if (splashDialog != null)
                splashDialog.hide();
            handler.removeCallbacks(hideTask);
            if (payList != null) {
                payList.clear();
            }
            if (waitList != null) {
                waitList.clear();
            }
            if (cancelList != null) {
                cancelList.clear();
            }
            LoginOutTools.goToLogin(app, MainActivity.this);
            manager.cancelAll();//将在通知栏的消息取消
        }
    }

    private void initView() {
        mViewPager = (MyViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
        title_text = (TextView) findViewById(R.id.title_text);
        title_textTab3 = (TextView) findViewById(R.id.title_textTab3);
        userImage = (ImageView) findViewById(R.id.user_image);
        titleR = (RelativeLayout) findViewById(R.id.titleR);
        titleR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImage.setImageResource(R.drawable.up);
                showSpinWindow();
            }
        });
        menu = (RelativeLayout) findViewById(R.id.menu);
        menuBackground = (TextView) findViewById(R.id.menuBackground);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem();
                mAdapter.notifyDataSetChanged();
                showMenuWindow();
            }
        });
        vouch = (RelativeLayout) findViewById(R.id.vouch);
        vouch.setVisibility(View.GONE);
        vouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCancel();
                Intent intent = new Intent(MainActivity.this, StatsUsedActivity.class);
                startActivity(intent);
            }
        });
        exit = (RelativeLayout) findViewById(R.id.exit);//退出
        exit.setVisibility(View.GONE);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) getApplication();
                String[] name = app.getTotalName().split("-");
                logoutDialog = new HintDialog(MainActivity.this, "提示", "确实要将当前账户“" + name[1] + "”退出登录吗？", "退出登录", new HintDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String query) {
                        if (query.equals("confirm")) {
                            if (payList != null) {
                                payList.clear();
                            }
                            if (waitList != null) {
                                waitList.clear();
                            }
                            if (cancelList != null) {
                                cancelList.clear();
                            }
                            App app = (App) getApplication();
                            LoginOutTools.signOutSimple(app, MainActivity.this);
                            editCancel();
                        }
                        logoutDialog.dismiss();
                    }
                });
                logoutDialog.setTitle("验证订单");
                logoutDialog.show();
            }
        });
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.tab_favorites:
                        menu.setVisibility(View.VISIBLE);
                        vouch.setVisibility(View.GONE);
                        exit.setVisibility(View.GONE);
                        title_textTab3.setVisibility(View.GONE);
                        titleR.setVisibility(View.VISIBLE);
                        String title1 = ((App) getApplication()).getTitle();
                        if (title1 != null) {
                            title_text.setText(title1);
                        }
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.tab_nearby:
                        editCancel();
                        menu.setVisibility(View.GONE);
                        exit.setVisibility(View.GONE);
                        title_textTab3.setVisibility(View.GONE);
                        vouch.setVisibility(View.VISIBLE);
                        titleR.setVisibility(View.VISIBLE);
                        String title2 = ((App) getApplication()).getTitle();
                        if (title2 != null) {
                            title_text.setText(title2);
                        }
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.tab_friends:
                        title_text.setText("账户");
                        editCancel();
                        menu.setVisibility(View.GONE);
                        vouch.setVisibility(View.GONE);
                        exit.setVisibility(View.VISIBLE);
                        title_textTab3.setVisibility(View.VISIBLE);
                        titleR.setVisibility(View.GONE);
                        mViewPager.setCurrentItem(2);
                        break;
                }
            }
        });
        bottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
            }
        });
    }

    private void bluetooth() {
        if (bluetoothHandler == null)
            bluetoothHandler = BluetoothHandler.newInstance(MainActivity.this, mHandler);
        if (bluetoothHandler.bluetoothAdapter != null) {
            boolean enabled = bluetoothHandler.bluetoothAdapter.isEnabled();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//如果 API level 是大于等于 23(Android 5.0) 时
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10001);
                } else {
                    if (enabled) {
                        bluetoothHandler.start();
                        autoPrint();
                    } else {
                        openBluetooth();
                    }
                }
            } else {
                if (enabled) {
                    bluetoothHandler.start();
                    autoPrint();
                } else {
                    openBluetooth();
                }
            }
            Intent service = new Intent(MainActivity.this, BluetoothService.class);
            service.setPackage(getPackageName());
            MainActivity.this.stopService(service);
        } else {
            Alerter.create(MainActivity.this)
                    .setText("此设备不支持蓝牙功能")
                    .setBackgroundColorRes(R.color.alerter_confirm)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
                if (bluetoothHandler.bluetoothAdapter.isEnabled()) autoPrint();
                else openBluetooth();
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
                Alerter.create(MainActivity.this)
                        .setText("蓝牙开启失败")
                        .setBackgroundColorRes(R.color.alerter_confirm)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void openBluetooth() {
        Alerter.create(this)
                .setText("正在开启蓝牙，请稍后重试")
                .setBackgroundColorRes(R.color.alerter_info)
                .show();
        bluetoothHandler.bluetoothAdapter.enable();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                bluetoothHandler.start();
            }
        };
        timer.schedule(task, 1000); //2秒后
    }

    private void inVariable() {
        fragmentList = new ArrayList<>();
        orderListFragment = new OrderListFragment();
        Fragment verifyFragment = new VerifyFragment();
        Fragment userFragment = new UserFragment();
        fragmentList.add(orderListFragment);
        fragmentList.add(verifyFragment);
        fragmentList.add(userFragment);
        mViewPager.setAdapter(new MyPageAdapter(getSupportFragmentManager()));
    }

    public void editCancel() {
        EditText editText = orderListFragment.searchTextList;
        if (editText != null) {
            editText.setText("");//搜索search
            InputMethodTool.cancelInput(editText);
        }
    }

    private void setMenuItem() {
        menuItems.clear();
        MenuItem menuItem = new MenuItem();
        menuItem.setIconRes(R.mipmap.collection);
        menuItem.setMenuName("收款");
        menuItems.add(menuItem);
        menuItem = new MenuItem();
        menuItem.setIconRes(R.mipmap.auto_print);
        App app = (App) getApplication();
        if (app.getChecked().equals("false")) {
            menuItem.setMenuName("自动打印（关）");
        } else {
            menuItem.setMenuName("自动打印（开）");
        }
        menuItems.add(menuItem);
    }

    private void popWindowUserList() {
        switchUserBeanArrayList = switchUserList();
        int addNum = switchUserBeanArrayList.size();
        SwitchUserBean bean = new SwitchUserBean();
        bean.setTitle("其他帐号");
        bean.setToken("noToken");
        switchUserBeanArrayList.add(addNum, bean);
        SpinerAdapter mAdapter = new SpinerAdapter(this, switchUserBeanArrayList);
        mAdapter.refreshData(switchUserBeanArrayList, 0);
        mSpinnerPopWindow = new SpinnerPopWindow(this, 1);
        mSpinnerPopWindow.setTitleAdapter(mAdapter);
        mSpinnerPopWindow.setItemTitleListener(this);
        mSpinnerPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                userImage.setImageResource(R.drawable.down);
                menuBackground.setVisibility(View.GONE);//菜单收回时隐藏背景
            }
        });

    }

    private void popMenuList() {
        setMenuItem();
        mAdapter = new SpinnerMenuAdapter(this, menuItems);
        menuSpinner = new SpinnerPopWindow(this, 2);
        menuSpinner.setMenuAdapter(mAdapter);
        menuSpinner.setItemMenuListener(this);
        menuSpinner.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                menuBackground.setVisibility(View.GONE);//菜单收回时隐藏背景
            }
        });
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(MainActivity.this);
        return cameraSql.queryDataToSQLite();
    }

    private void showSpinWindow() {
        popWindowUserList();

        menuBackground.setVisibility(View.VISIBLE);
        mSpinnerPopWindow.showAsDropDown(titleR);
    }

    private void showMenuWindow() {
        menuBackground.setVisibility(View.VISIBLE);
        menuSpinner.showAsDropDown(menu);
    }

    @Override
    public void onItemClick(final int pos) {
        editCancel();
        if (switchUserBeanArrayList.size() == (pos + 1)) {
            App app = (App) getApplication();
            LoginOutTools.loginSimple(app, MainActivity.this);
        } else {
            checkOtherLogin(pos, false);
        }
    }

    private void checkOtherLogin(final int position, final boolean pos) {
        loadToast = new LoadToast(MainActivity.this);
        loadToast.setText("正在检测账号");
        loadToast.setTranslationY(100);
        loadToast.show();
        final App app = (App) getApplication();
        app.getApiService().checkLogin(switchUserBeanArrayList.get(position).getToken(), new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 200) {
                        loginTimeOutCheck = "no";
                        if (loadToast != null) loadToast.success();
                        if (pos) {
                            String[] name = switchUserBeanArrayList.get(position).getTitle().split("-");
                            if ("back".equals(exitCome)) {
                                Toast.makeText(MainActivity.this, "正为您切换帐号：" + name[1], Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "之前帐号已登录过期，正为您切换帐号：" + name[1], Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (resultMessage.getResult().equals("ok")) {
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            app.setShowList(resultMessage.getAllow_show_list());
                            app.setUserId(switchUserBeanArrayList.get(position).getId());
                            app.setLoginName(switchUserBeanArrayList.get(position).getLoginName());
                            app.setAllowPay(resultMessage.getAllow_pay());
                            App app = (App) getApplication();
                            String title = switchUserBeanArrayList.get(position).getTitle();
                            String token = String.valueOf(position);
                            if (title.indexOf("-") > 0) {
                                String[] s1 = title.split("-");
                                title_text.setText(s1[0]);
                                app.setTitle(s1[0]);
                            } else {
                                title_text.setText(title);
                                app.setTitle(title);
                            }
                            ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
                            switchUserBeanArrayList = switchUserList();
                            if (payList != null) {
                                payList.clear();
                            }
                            if (waitList != null) {
                                waitList.clear();
                            }
                            if (cancelList != null) {
                                cancelList.clear();
                            }
                            app.setToken(switchUserBeanArrayList.get(Integer.parseInt(token)).getToken());
                            if ("true".equals(app.getShowList())) {
                                Intent intent = new Intent();
                                intent.setAction("callRefresh");
                                intent.putExtra("refresh", "true");
                                intent.putExtra("tab3refresh", "true");
                                intent.putExtra("jump", "false");
                                sendBroadcast(intent);
                            } else {
                                if (splashDialog != null)
                                    splashDialog.hide();
                                handler.removeCallbacks(hideTask);
                                app.setShowList("false");
                                editCancel();
                                Intent intent = new Intent(MainActivity.this, SimpleMainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            Toast.makeText(MainActivity.this, "切换成功", Toast.LENGTH_SHORT).show();
                        }
                    } else if (resultMessage.getErrorCode() == 401) {
                        if ("back".equals(loginTimeOutCheck)) {
                            if (switchUserBeanArrayList.size() > 1) {
                                LoginOutTools.deleteUser(MainActivity.this, switchUserBeanArrayList.get(position).getId());//登录过期的删掉
                                switchUserBeanArrayList.clear();
                                switchUserBeanArrayList = switchUserList();
                                if (switchUserBeanArrayList.size() > 0) {
                                    checkOtherLogin(0, true);
                                } else {
                                    LoginOutTools.goToLogin(app, MainActivity.this);
                                }
                            } else {
                                if (loadToast != null) loadToast.error();
                                LoginOutTools.loginPastDue(app, MainActivity.this, switchUserBeanArrayList.get(position).getId(), switchUserBeanArrayList.get(position).getLoginName());
                                Toast.makeText(MainActivity.this, "其他帐号已登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (loadToast != null) loadToast.error();
                            if (splashDialog != null)
                                splashDialog.hide();
                            handler.removeCallbacks(hideTask);
                            if (app.getLoginTab() == 0) {
                                LoginOutTools.loginPastDue(app, MainActivity.this, switchUserBeanArrayList.get(position).getId(), switchUserBeanArrayList.get(position).getLoginName());
                                Toast.makeText(MainActivity.this, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                            }
                            editCancel();
                        }
                    }
                }
                if ("back".equals(exitCome)) {//是否显示列表
                    userImage.setImageResource(R.drawable.up);
                    showSpinWindow();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (splashDialog != null)
                    splashDialog.hide();
                handler.removeCallbacks(hideTask);
                App app = (App) MainActivity.this.getApplication();
                app.setChecked("false");
                app.setJump("false");
                editCancel();
                if (app.getLoginTab() == 0) {
                    LoginOutTools.loginPastDue(app, MainActivity.this, switchUserBeanArrayList.get(position).getId(), switchUserBeanArrayList.get(position).getLoginName());
                    Alerter.create(MainActivity.this)
                            .setText("切换失败，请重新登录")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
            }
        });
    }

    @Override
    public void onItemMenuClick(int pos) {
        App app = (App) getApplication();
        switch (pos) {
            case 0://收款
                String allow_pay = app.getAllowPay();
                if (allow_pay != null) {
                    if ("true".equals(allow_pay)) {
                        editCancel();
                        Intent intent = new Intent(MainActivity.this, AllowPayActivity.class);
                        startActivity(intent);
                        //finish();
                    } else {
                        Alerter.create(MainActivity.this)
                                .setText("没有收款权限" + allow_pay)
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                } else {
                    Alerter.create(MainActivity.this)
                            .setText("未获取到权限数据，请重新登录")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
                break;
            case 1://自动打印
                app = (App) getApplication();
                if ("false".equals(app.getChecked())) {
                    // item.setChecked(true);
                    BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mAdapter != null && mAdapter.isEnabled()) {
                        if (bluetoothHandler != null) {
                            bluetoothHandler.stop();
                            bluetoothHandler = BluetoothHandler.newInstance(this, mHandler);
                            bluetoothHandler.start();
                        } else {
                            bluetoothHandler = BluetoothHandler.newInstance(this, mHandler);
                            bluetoothHandler.start();
                        }
                        bluetooth();
                    } else {
                        if (mAdapter != null) {
                            Alerter.create(MainActivity.this)
                                    .setText("正在打开蓝牙")
                                    .setBackgroundColorRes(R.color.alerter_info)
                                    .show();
                            mAdapter.enable();
                        }
                    }
                } else {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
                    app.setChecked("false");
                    B_item = null;
                    Intent intent = new Intent(MainActivity.this, ScheduledExecutorService.class);
                    intent.setPackage(getPackageName());
                    MainActivity.this.startService(intent);
                    //停止每隔10s启动service的工作
                    Alerter.create(MainActivity.this)
                            .setText("自动打印已关闭")
                            .setBackgroundColorRes(R.color.alerter_info)
                            .show();
                }
                break;
        }
    }

    public void autoPrint() {
        bluetoothHandler.showDeviceList(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                B_item = bluetoothHandler.getAdapter().getItem(position);
                App app = (App) getApplication();
                app.setPos(position);
                Alerter.create(MainActivity.this)
                        .setText("自动打印已开启")
                        .setBackgroundColorRes(R.color.alerter_info)
                        .show();
                app.setItem(B_item);
                ArrayList<OrderBean> orderList = new ArrayList<OrderBean>();
                OrderBean orderBean = new OrderBean();
                if (payList != null && payList.size() > 0) {
                    orderBean.setId(payList.get(0).getId());
                    orderBean.setTitle(payList.get(0).getTitle());
                    orderBean.setTotalPrice(payList.get(0).getTotalPrice());
                    orderBean.setFundsAdjust(payList.get(0).getFundsAdjust());
                    orderBean.setCreatedAt(payList.get(0).getCreatedAt());
                    orderBean.setPurchasedAt(payList.get(0).getPurchasedAt());
                    orderBean.setCancelledAt(payList.get(0).getCancelledAt());
                    orderBean.setMobile(payList.get(0).getMobile());
                    orderBean.setAddress(payList.get(0).getAddress());
                    orderBean.setUsed(payList.get(0).getUsed());
                    orderBean.setCustomer_note(payList.get(0).getCustomer_note());
                    orderBean.setRefunded(payList.get(0).getRefunded());
                    orderList.add(orderBean);
                }
                OrderSql address = new OrderSql(MainActivity.this);
                address.createTable();
                address.setDataToSQLite(orderList);//对地址数据信息进行更新

                Intent intent = new Intent(MainActivity.this, ScheduledExecutorService.class);
                intent.setPackage(getPackageName());
                MainActivity.this.startService(intent);

                app.setChecked("true");
            }
        });

    }

    private class MyPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        MyPageAdapter(FragmentManager fm) {
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

        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothHandler.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothHandler.STATE_CONNECTED:   //已连接
                            //启动服务
                            break;
                        case BluetoothHandler.STATE_CONNECTING:  //正在连接
                            break;
                        case BluetoothHandler.STATE_LISTEN:     //监听连接的到来
                        case BluetoothHandler.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothHandler.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    break;
                case BluetoothHandler.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Alerter.create(MainActivity.this)
                            .setText(R.string.bluetooth_error)
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
            }
        }

    };

    private long firstClick = -1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long l = System.currentTimeMillis() - firstClick;
            if (firstClick == -1) {
                firstClick = System.currentTimeMillis();
                Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
            } else if (l < 1000) {
                if (payList != null) {
                    payList.clear();
                }
                if (waitList != null) {
                    waitList.clear();
                }
                if (cancelList != null) {
                    cancelList.clear();
                }
                finish();
            } else {
                firstClick = -1;
            }
        }
        return false;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("refresh") != null) {
                String refresh = intent.getExtras().getString("refresh");
                if (refresh != null) {
                    if ("true".equals(refresh)) {
                        App app = (App) getApplication();
                        title_text.setText(app.getTitle());
                    }
                }
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (splashDialog != null)
            splashDialog.hide();
        handler.removeCallbacks(hideTask);
        unregisterReceiver(broadcastReceiverLive);
    }
}
