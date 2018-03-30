package com.wetoop.storeoperator.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bottombar.BottomBar;
import com.wetoop.storeoperator.bottombar.OnTabReselectListener;
import com.wetoop.storeoperator.bottombar.OnTabSelectListener;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.adapter.SpinerAdapter;
import com.wetoop.storeoperator.ui.dialog.HintDialog;
import com.wetoop.storeoperator.ui.dialog.LoadingDialog;
import com.wetoop.storeoperator.ui.dialog.SplashDialog;
import com.wetoop.storeoperator.ui.fragment.UserFragment;
import com.wetoop.storeoperator.ui.fragment.VerifyFragment;
import com.wetoop.storeoperator.ui.widget.MyViewPager;
import com.wetoop.storeoperator.ui.widget.SpinnerPopWindow;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;


public class SimpleMainActivity extends AppCompatActivity implements SpinerAdapter.IOnItemSelectListener {

    public String checked_nn = null;
    private TextView title_text, title_textTab3;
    private static int pos = 0;
    private RelativeLayout exit, titleR, vouch;
    private MyViewPager mViewPager;
    private ImageView userImage;
    public TextView menuBackground;
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private ArrayList<Fragment> fragmentList;
    private SpinnerPopWindow mSpinerPopWindow;
    private MyBroadcastReceiver broadcastReceiverLive;
    private String exitCome = "no";//用于标记是否要显示选择账号列表
    private String back = "no";//用于标记是否有可切换账号
    private SplashDialog splashDialog;
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
        setContentView(R.layout.activity_simple_main);
        setWindowStatusBarColor(SimpleMainActivity.this, R.color.title_clicked_color);
        if (App.showLoading) {
            App.showLoading = false;
            this.splashDialog = new SplashDialog(this);
            splashDialog.show();
            handler.postDelayed(hideTask, 1500);
        }
        initView();
        inVariable();
        setViewListener();
        popWindowUserList();
        setViewData();
    }

    private void initView() {
        vouch = (RelativeLayout) findViewById(R.id.vouch);
        exit = (RelativeLayout) findViewById(R.id.exit);//退出
        exit.setVisibility(View.GONE);
        title_text = (TextView) findViewById(R.id.title_text);
        title_textTab3 = (TextView) findViewById(R.id.title_textTab3);
        titleR = (RelativeLayout) findViewById(R.id.titleR);
        userImage = (ImageView) findViewById(R.id.user_image);
        menuBackground = (TextView) findViewById(R.id.menuBackground);
        mViewPager = (MyViewPager) findViewById(R.id.pager);
    }

    private void setViewData() {
        App app = (App) getApplication();
        app.addActivity(this);
        broadcastReceiverLive = new MyBroadcastReceiver();
        IntentFilter intentFilterLive = new IntentFilter("callRefresh");
        registerReceiver(broadcastReceiverLive, intentFilterLive);
        String title = ((App) getApplication()).getTitle();
        if (title != null) {
            title_text.setText(title);
        }
        Intent intent = getIntent();
        if(intent.getStringExtra("checked_N") != null)
            checked_nn = intent.getStringExtra("checked_N");
        if(intent.getStringExtra("exitCome")!=null)
            exitCome = intent.getStringExtra("exitCome");
        if(intent.getStringExtra("loginBack")!=null){
            back = intent.getStringExtra("loginBack");
            if("back".equals(intent.getStringExtra("loginBack"))){
                ArrayList<SwitchUserBean> switchUserLoginBackList = switchUserList();
                if(switchUserLoginBackList.size() > 0) {
                    checkOtherLogin(0,true);
                }
            }
        }
    }

    private void setViewListener(){
        vouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SimpleMainActivity.this, StatsUsedActivity.class);
                startActivity(intent);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App)getApplication();
                String[] name = app.getTotalName().split("-");
                logoutDialog = new HintDialog(SimpleMainActivity.this, "提示", "是否要退出帐户："+name[1]+"？", "退出登录", new HintDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String query) {
                        if (query.equals("confirm")) {
                            App app = (App) getApplication();
                            LoginOutTools.signOutSimple(app,SimpleMainActivity.this);
                        }
                        logoutDialog.dismiss();
                    }
                });
                logoutDialog.setTitle("验证订单");
                logoutDialog.show();
            }
        });
        titleR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImage.setImageResource(R.drawable.up);
                showSpinWindow();
            }
        });
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.simple_tab_nearby:
                        vouch.setVisibility(View.VISIBLE);
                        exit.setVisibility(View.GONE);
                        title_textTab3.setVisibility(View.GONE);
                        titleR.setVisibility(View.VISIBLE);
                        String title = ((App) getApplication()).getTitle();
                        if (title != null) {
                            title_text.setText(title);
                        }
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.simple_tab_friends:
                        title_text.setText("账户");
                        vouch.setVisibility(View.GONE);
                        exit.setVisibility(View.VISIBLE);
                        title_textTab3.setVisibility(View.VISIBLE);
                        titleR.setVisibility(View.GONE);
                        mViewPager.setCurrentItem(1);
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

    private void inVariable() {
        fragmentList = new ArrayList<>();
        Fragment tab2 = new VerifyFragment();
        Fragment tab3 = new UserFragment();
        fragmentList.add(tab2);
        fragmentList.add(tab3);
        mViewPager.setAdapter(new myPageAdapter(getSupportFragmentManager()));
    }

    private void popWindowUserList() {
        switchUserBeanArrayList = switchUserList();
        int addNum = switchUserBeanArrayList.size();
        SwitchUserBean bean = new SwitchUserBean();
        bean.setTitle("切换其他帐号");
        bean.setToken("noToken");
        switchUserBeanArrayList.add(addNum, bean);
        SpinerAdapter mAdapter = new SpinerAdapter(this, switchUserBeanArrayList);
        mAdapter.refreshData(switchUserBeanArrayList, 0);
        mSpinerPopWindow = new SpinnerPopWindow(this, 1);
        mSpinerPopWindow.setTitleAdapter(mAdapter);
        mSpinerPopWindow.setItemTitleListener(this);
        mSpinerPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                userImage.setImageResource(R.drawable.down);
                menuBackground.setVisibility(View.GONE);//菜单收回时隐藏背景
            }
        });
    }

    private void showSpinWindow() {
        menuBackground.setVisibility(View.VISIBLE);
        mSpinerPopWindow.showAsDropDown(titleR);
    }

    private void checkOtherLogin(final int position, final boolean posBool) {
        loadToast = new LoadToast(SimpleMainActivity.this);
        loadToast.setText("正在检测账号");
        loadToast.setTranslationY(100);
        loadToast.show();
        final App app = (App) getApplication();
        app.getApiService().checkLogin(switchUserBeanArrayList.get(position).getToken(), new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 200) {
                        if(loadToast != null) loadToast.success();
                        if (posBool) {
                            String[] name = switchUserBeanArrayList.get(position).getTitle().split("-");
                            if("back".equals(exitCome)){
                                Alerter.create(SimpleMainActivity.this)
                                        .setText("正为您切换帐号："+name[1])
                                        .setBackgroundColorRes(R.color.alerter_info)
                                        .show();
                            }else{
                                Alerter.create(SimpleMainActivity.this)
                                        .setText("之前帐号已登录过期，正为您切换帐号："+name[1])
                                        .setBackgroundColorRes(R.color.alerter_info)
                                        .show();
                            }
                        }
                        if (resultMessage.getResult().equals("ok")) {
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            app.setLoginName(switchUserBeanArrayList.get(position).getLoginName());
                            app.setShowList(resultMessage.getAllow_show_list());
                            app.setUserId(switchUserBeanArrayList.get(position).getId());
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
                            app.setToken(switchUserBeanArrayList.get(Integer.parseInt(token)).getToken());
                            if (app.getShowList().equals("true")) {
                                Intent intent = new Intent(SimpleMainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent();
                                intent.setAction("callRefresh");
                                intent.putExtra("refresh", "false");
                                intent.putExtra("tab3refresh", "true");
                                sendBroadcast(intent);
                            }
                        }
                    } else if (resultMessage.getErrorCode() == 401) {
                        if("back".equals(back)){
                            if(switchUserBeanArrayList.size() > 1) {
                                LoginOutTools.deleteUser(SimpleMainActivity.this,switchUserBeanArrayList.get(position).getId());//登录过期的删掉
                                switchUserBeanArrayList.clear();
                                switchUserBeanArrayList = switchUserList();
                                checkOtherLogin(0, true);
                            }else{
                                if(loadToast != null) loadToast.error();
                                LoginOutTools.loginPastDue(app, SimpleMainActivity.this, switchUserBeanArrayList.get(position).getId(), switchUserBeanArrayList.get(position).getLoginName());
                                Toast.makeText(SimpleMainActivity.this, "其他帐号已登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            if(loadToast != null) loadToast.error();
                            if (splashDialog != null)
                                splashDialog.hide();
                            handler.removeCallbacks(hideTask);
                            LoginOutTools.loginPastDue(app, SimpleMainActivity.this, switchUserBeanArrayList.get(position).getId(), switchUserBeanArrayList.get(position).getLoginName());
                            Toast.makeText(SimpleMainActivity.this, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if("back".equals(exitCome)){//是否显示列表
                    userImage.setImageResource(R.drawable.up);
                    showSpinWindow();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                LoginOutTools.loginPastDue(app,SimpleMainActivity.this,switchUserBeanArrayList.get(pos).getId(),switchUserBeanArrayList.get(pos).getLoginName());
                Alerter.create(SimpleMainActivity.this)
                        .setText("切换失败，请重新登录")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    @Override
    public void onItemClick(final int pos) {
        if (switchUserBeanArrayList.size() == (pos + 1)) {
            App app = (App) getApplication();
            LoginOutTools.loginSimple(app, SimpleMainActivity.this);
        } else {
            checkOtherLogin(pos, false);
        }
    }

    private class myPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        public myPageAdapter(FragmentManager fm) {
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

    @Override
    protected void onResume() {
        super.onResume();
        final App app = (App) getApplication();
        Intent checked = getIntent();
        checked_nn = checked.getStringExtra("checked_N");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (app.isSignedIn()) {
            if("no".equals(back)) {
                app.getApiService().checkLogin(app.getToken(), new Callback<UserInfo>() {

                    @Override
                    public void success(UserInfo resultMessage, Response response) {
                        if (resultMessage != null) {
                            if (resultMessage.getErrorCode() == 401) {
                                if (splashDialog != null)
                                    splashDialog.hide();
                                handler.removeCallbacks(hideTask);
                                LoginOutTools.loginPastDue(app, SimpleMainActivity.this, app.getUserId(), app.getLoginName());
                                Alerter.create(SimpleMainActivity.this)
                                        .setText("登录过期，请重新登录")
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        LoginOutTools.loginPastDue(app, SimpleMainActivity.this, app.getUserId(), app.getLoginName());
                        Alerter.create(SimpleMainActivity.this)
                                .setText("切换失败，请重新登录")
                                .setBackgroundColorRes(R.color.alerter_alert)
                                .show();
                    }
                });
            }
        } else {
            handler.removeCallbacks(hideTask);
            if(splashDialog != null)
                splashDialog.hide();
            LoginOutTools.goToLogin(app,SimpleMainActivity.this);
            if (manager != null) {
                manager.cancelAll();//点击取消自动打印时，将在通知栏的消息取消
            }
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(SimpleMainActivity.this);
        return cameraSql.queryDataToSQLite();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("refresh") != null) {
                String refresh = intent.getExtras().getString("refresh");
                if (refresh != null) {
                    if (refresh.equals("true")) {
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
        unregisterReceiver(broadcastReceiverLive);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return false;
    }
}
