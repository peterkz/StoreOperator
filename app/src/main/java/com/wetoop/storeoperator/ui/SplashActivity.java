package com.wetoop.storeoperator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;

/**
 * @author Parck.
 * @date 2017/9/29.
 * @desc
 */
public class SplashActivity extends AppCompatActivity {

    private App app;
    private Handler handler = new Handler();
    private long loadingTime = 0;
    private long loadingStartTime = System.currentTimeMillis();
    private final long TIMEOUT_TIME = 8 * 1000;
    private final long WAIT_TIME = 0;
    private Runnable loginTimeoutTask;
    private Runnable toMainPagerTask = new Runnable() {
        @Override
        public void run() {
            if (loginTimeoutTask != null) handler.removeCallbacks(loginTimeoutTask);
            startActivity(new Intent(SplashActivity.this, "true".equalsIgnoreCase(app.getShowList()) || "".equals(app.getShowList()) ? MainActivity.class : SimpleMainActivity.class));
            finish();
        }
    };

    private Runnable toLoginPagerTask = new Runnable() {
        @Override
        public void run() {
            if (loginTimeoutTask != null) handler.removeCallbacks(loginTimeoutTask);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.showLoading = true;
        super.onCreate(savedInstanceState);
        setWindowStatusBarColor(SplashActivity.this, R.color.title_clicked_color);
        setContentView(R.layout.activity_splash);
        init();
    }

    private void init() {
        app = (App) getApplication();
        if (app.getFirstUsed() <= 5) createDataBase();
        else
            startToMainPagerTask();
    }

    /**
     * 创建数据库
     */
    private void createDataBase() {
        SwitchUserSql switchUserSql = new SwitchUserSql(this);
        boolean createTable = switchUserSql.createTable();
        if (createTable) {
            app.setFirstUsed(6);
            app.signOut();
            finish();
            app.setChecked("false");
        }
        loadingTime = System.currentTimeMillis() - loadingStartTime;
        startToLoginPagerTask();
    }

    /**
     * 认证登录
     */
    @Deprecated
    private void checkAccess() {
        // 验证身份
        ArrayList<SwitchUserBean> switchUserBeen = switchUserList();
        if (switchUserBeen != null && switchUserBeen.size() > 0) {
            app.getApiService().checkLogin(switchUserBeen.get(0).getToken(), new Callback<UserInfo>() {
                @Override
                public void success(UserInfo result, Response response) {
                    loadingTime = System.currentTimeMillis() - loadingStartTime;
                    if (result.getErrorCode() == 200 && "OK".equalsIgnoreCase(result.getResult())) {
                        startToMainPagerTask();
                    } else {
                        startToLoginPagerTask();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    startToLoginPagerTask();
                }
            });
            // 开始登录超时任务
            startLoginTimeoutTask();
        } else {
            startToLoginPagerTask();
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        ArrayList<SwitchUserBean> cameraList;
        SwitchUserSql cameraSql = new SwitchUserSql(this);
        cameraList = cameraSql.queryDataToSQLite();
        return cameraList;
    }

    /**
     * 跳转主页任务
     */
    public void startToMainPagerTask() {
        handler.postDelayed(toMainPagerTask, loadingTime > WAIT_TIME ? 0 : WAIT_TIME - loadingTime); // 保证 8s >= 界面展示时间 >= 2s
    }

    /**
     * 跳转登录页面任务
     */
    public void startToLoginPagerTask() {
        handler.postDelayed(toLoginPagerTask, loadingTime > WAIT_TIME ? 0 : WAIT_TIME - loadingTime); // 保证 8s >= 界面展示时间 >= 2s
    }

    /**
     * 登录超时任务
     */
    private void startLoginTimeoutTask() {
        loginTimeoutTask = new Runnable() {
            @Override
            public void run() {
                if (toMainPagerTask != null) handler.removeCallbacks(toMainPagerTask);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                Alerter.create(SplashActivity.this)
                        .setText("登录超时")
                        .setBackgroundColorRes(R.color.alerter_confirm)
                        .show();
            }
        };
        handler.postDelayed(loginTimeoutTask, TIMEOUT_TIME);
    }
}
