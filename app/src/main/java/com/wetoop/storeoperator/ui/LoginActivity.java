package com.wetoop.storeoperator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.ResultMessage;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.CountDownTool;
import com.wetoop.storeoperator.tools.InputMethodTool;
import com.wetoop.storeoperator.tools.SqlLoginTool;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUserView, mPasswordView;
    private ImageView loginBack;
    private View mProgressView;
    private View mLoginFormView;
    private boolean upData = true;
    private String backToMain = "no";//用于是否返回main的标记
    private String switchUser = "no";//用于标记是否来自“切换其他账号”
    private String exit = "no";//用于标记是否来自“退出账号”
    private String allowFinish = "no";//标记使用来自收款登录过期
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private CountDownTool countDownTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        setViewListener();
        Intent intent = getIntent();
        if (intent.getStringExtra("finish") != null) {
            if (intent.getStringExtra("finish").equals("true")) {
                Alerter.create(this)
                        .setText(R.string.login_failure)
                        .setBackgroundColorRes(R.color.alerter_confirm)
                        .show();
            }
        }
        if (intent.getStringExtra("outLogin") != null) {
            if (!intent.getStringExtra("outLogin").equals(""))
                backToMain = intent.getStringExtra("outLogin");
        }
        if (intent.getStringExtra("userName") != null) {
            mUserView.setText(intent.getStringExtra("userName"));
            mUserView.setFocusableInTouchMode(false);
            mPasswordView.setFocusableInTouchMode(true);
        } else {
            mUserView.setFocusableInTouchMode(true);
        }
        if (intent.getStringExtra("switchUser") != null)
            switchUser = intent.getStringExtra("switchUser");
        if (intent.getStringExtra("exit") != null)
            exit = intent.getStringExtra("exit");
        if (intent.getStringExtra("allow_finish") != null)
            allowFinish = intent.getStringExtra("allow_finish");
        if ("no".equals(backToMain)) {
            loginBack.setVisibility(View.INVISIBLE);
        } else {
            loginBack.setVisibility(View.VISIBLE);
            switchUserBeanArrayList.clear();
            switchUserBeanArrayList = switchUserList();
            App app = (App) getApplication();
            if (switchUserBeanArrayList.size() <= 0)
                app.signOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTool.finish();
    }

    private void initView() {
        mUserView = (AutoCompleteTextView) findViewById(R.id.userName);
        mPasswordView = (AutoCompleteTextView) findViewById(R.id.password);
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        loginBack = (ImageView) findViewById(R.id.loginBack);
    }

    private Button signInButton;
    private void setViewListener() {
        loginBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchUserBeanArrayList.clear();
                switchUserBeanArrayList = switchUserList();
                if (switchUserBeanArrayList.size() > 0) {
                    if(!"no".equals(allowFinish)){
                        finish();
                        return;
                    }
                    if (backToMain.equals("false")) {
                        Intent intent = new Intent(LoginActivity.this, SimpleMainActivity.class);
                        if ("no".equals(switchUser))
                            intent.putExtra("loginBack", "back");
                        if ("come".equals(exit))
                            intent.putExtra("exitCome", "back");
                        startActivity(intent);
                    } else if (backToMain.equals("true")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        if ("no".equals(switchUser))
                            intent.putExtra("loginBack", "back");
                        if ("come".equals(exit))
                            intent.putExtra("exitCome", "back");
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Alerter.create(LoginActivity.this)
                            .setText(R.string.login_no_user)
                            .setBackgroundColorRes(R.color.alerter_info)
                            .show();
                }
            }
        });
        mUserView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserView.setFocusableInTouchMode(true);
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mUserView.setFocusableInTouchMode(true);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        signInButton = (Button) findViewById(R.id.sign_in_button);
        countDownTool = new CountDownTool(signInButton);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    public void attemptLogin() {
        mUserView.setError(null);
        mPasswordView.setError(null);
        final String username = mUserView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("密码不能为空");
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUserView.setError("用户名不能为空");
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            InputMethodTool.cancelInput(mPasswordView);
            showProgress(true);
            final App app = (App) getApplication();
            app.getApiService().signIn(username, password, new Callback<ResultMessage>() {
                @Override
                public void success(ResultMessage resultMessage, Response response) {
                    if (resultMessage != null) {
                        if (resultMessage.getErrorCode() == 200) {
                            app.setLoginName(username);
                            app.setToken(resultMessage.getResult());
                            app.setTitle(resultMessage.getMessage());
                            app.setAllowPay(resultMessage.getAllow_pay());
                            app.setShowList(resultMessage.getAllow_show_list());
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            if (app.getToken().length() > 0) {
                                App app = (App) getApplication();
                                showProgress(false);
                                Alerter.create(LoginActivity.this)
                                        .setText(R.string.login_succeed)
                                        .setBackgroundColorRes(R.color.alerter_info)
                                        .show();
                                int clearList = 0;//用于控制是否清空main里的list
                                if (app.getUserId().equals("")) {
                                    app.setUserId(resultMessage.getUser_id());
                                    SqlLoginTool.insertSql(LoginActivity.this,resultMessage.getUser_id(),resultMessage.getResult(),title_name,username);
                                } else {
                                    switchUserBeanArrayList = switchUserList();
                                    for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
                                        if (resultMessage.getUser_id().equals(switchUserBeanArrayList.get(i).getId())) {
                                            upData = false;
                                        }
                                    }
                                    if (upData) {
                                        app.setUserId(resultMessage.getUser_id());
                                        SqlLoginTool.insertSql(LoginActivity.this,resultMessage.getUser_id(),resultMessage.getResult(),title_name,username);
                                        clearList = 1;
                                    } else {
                                        app.setUserId(resultMessage.getUser_id());
                                        SqlLoginTool.updateSql(LoginActivity.this,resultMessage.getUser_id(),resultMessage.getResult(),username);
                                        clearList = 1;
                                    }
                                }
                                if(!"no".equals(allowFinish)){
                                    finish();
                                    return;
                                }
                                if (resultMessage.getAllow_show_list().equals("true")) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("testCheck", "null");
                                    intent.putExtra("clearList", clearList);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, SimpleMainActivity.class);
                                    intent.putExtra("testCheck", "null");
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        } else {
                            mUserView.setFocusableInTouchMode(false);
                            mPasswordView.setFocusableInTouchMode(true);
                            countDownTool.start();
                            showProgress(false);
                            Alerter.create(LoginActivity.this)
                                    .setText(resultMessage.getErrorMessage())
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                            mUserView.setFocusableInTouchMode(true);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    showProgress(false);
                    Alerter.create(LoginActivity.this)
                            .setText(app.getErrorMessage(error))
                            .setBackgroundColorRes(R.color.alerter_alert)
                            .show();
                }
            });
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        ArrayList<SwitchUserBean> cameraList;
        SwitchUserSql cameraSql = new SwitchUserSql(LoginActivity.this);
        cameraList = cameraSql.queryDataToSQLite();
        return cameraList;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switchUserBeanArrayList.clear();
            switchUserBeanArrayList = switchUserList();
            if (switchUserBeanArrayList.size() > 0) {
                if(!"no".equals(allowFinish)){
                    finish();
                    return false;
                }
                if (backToMain.equals("false")) {
                    Intent intent = new Intent(LoginActivity.this, SimpleMainActivity.class);
                    if ("no".equals(switchUser))
                        intent.putExtra("loginBack", "back");
                    if ("come".equals(exit))
                        intent.putExtra("exitCome", "back");
                    startActivity(intent);
                } else if (backToMain.equals("true")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    if ("no".equals(switchUser))
                        intent.putExtra("loginBack", "back");
                    if ("come".equals(exit))
                        intent.putExtra("exitCome", "back");
                    startActivity(intent);
                }
            }
            finish();
        }
        return false;
    }
}

