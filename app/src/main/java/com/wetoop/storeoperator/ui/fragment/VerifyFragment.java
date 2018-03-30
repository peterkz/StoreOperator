package com.wetoop.storeoperator.ui.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.AllowPayActivity;
import com.wetoop.storeoperator.ui.MainActivity;
import com.wetoop.storeoperator.ui.OrderDetailActivity;
import com.wetoop.storeoperator.ui.ScannerActivity;
import com.wetoop.storeoperator.ui.dialog.HintDialog;
import com.wetoop.storeoperator.ui.dialog.SearchDialog;
import com.wetoop.storeoperator.utils.PermissionUtil;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.ui.fragment.OrderCancelFragment.cancelList;
import static com.wetoop.storeoperator.ui.fragment.OrderPayFragment.payList;
import static com.wetoop.storeoperator.ui.fragment.OrderWaitFragment.waitList;

/**
 * Created by Administrator on 2017/4/25.
 */

public class VerifyFragment extends Fragment {
    private RelativeLayout rScan, rInput, rPay;
    private SearchDialog searchDialog;
    private IntentFilter intentFilterLive;
    private MyBroadcastReceiver broadcastReceiverLive;
    private HintDialog logoutDialog1, logoutDialog2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_verfy, container, false);

        broadcastReceiverLive = new MyBroadcastReceiver();
        intentFilterLive = new IntentFilter("callRefresh");
        getActivity().registerReceiver(broadcastReceiverLive, intentFilterLive);
        rScan = (RelativeLayout) rootView.findViewById(R.id.rScan);
        rInput = (RelativeLayout) rootView.findViewById(R.id.rInput);
        rPay = (RelativeLayout) rootView.findViewById(R.id.rPay);
        rScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    showContacts();
                } else {
                    Intent intent = new Intent(getActivity(), ScannerActivity.class);
                    startActivity(intent);
                }
            }
        });
        rInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //"请输入订单码号查询"弹框
                searchDialog = new SearchDialog(getActivity(), new SearchDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String query) {
                        if (query != null) {
                            searchDialog.progressBar.setVisibility(View.VISIBLE);
                            searchDialog.comfirmBt.setVisibility(View.GONE);
                            if (TextUtils.isEmpty(query)) {
                                Alerter.create(getActivity())
                                        .setText("请输入订单号或代码")
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            } else {
                                String queryStr = query.toLowerCase();
                                App app = (App) getActivity().getApplication();
                                getOrderItemData(app.getToken(), queryStr);
                            }
                        }else{
                            searchDialog.dismiss();
                        }

                    }
                });
                searchDialog.setTitle("请输入订单码号查询");
                searchDialog.show();
            }
        });
        App app = (App) getActivity().getApplication();
        String allow_pay = app.getAllowPay();
        if (allow_pay != null) {
            if (allow_pay.equals("true")) {
                rPay.setVisibility(View.VISIBLE);
            } else {
                rPay.setVisibility(View.GONE);
            }
        } else {
            rPay.setVisibility(View.GONE);
        }
        rPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App app = (App) getActivity().getApplication();
                String allow_pay = app.getAllowPay();
                if (allow_pay != null) {
                    if (allow_pay.equals("true")) {
                        Intent intent = new Intent(getActivity(), AllowPayActivity.class);
                        startActivity(intent);
                        //finish();
                    } else {
                        Alerter.create(getActivity())
                                .setText("没有收款权限" + allow_pay)
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                } else {
                    Alerter.create(getActivity())
                            .setText("未获取到权限数据，请重新登录")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
            }
        });

        stringToJsonArray();

        return rootView;
    }

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

    private void getOrderItemData(final String token, final String getOrderId) {
        final App app = (App) getActivity().getApplication();
        app.getApiService().orderItem(token, getOrderId, json.toString(), new Callback<Order>() {
            @Override
            public void success(final Order order, Response response) {

                if (order != null) {
                    String itemId = order.getId();
                    if (itemId != null) {
                        if (itemId.startsWith(":")) {
                            final String[] s1 = itemId.split(":");
                            app.getApiService().orderItem(app.getToken(), s1[1], json.toString(), new Callback<Order>() {
                                @Override
                                public void success(Order order, Response response) {
                                    if (order != null) {
                                        String itemId = order.getId();
                                        searchDialog.progressBar.setVisibility(View.GONE);
                                        searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                                        searchDialog.dismiss();
                                        if (itemId != null) {
                                            Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                                            intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, order);
                                            if (order.getPurchasedAt() != null) {
                                                intent.putExtra("card", 3);//如果是已付款的订单，在进入详情时显示“使用”的按钮
                                            }
                                            intent.putExtra("id", s1[1]);
                                            startActivity(intent);
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    searchDialog.progressBar.setVisibility(View.GONE);
                                    searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                                    searchDialog.dismiss();
                                }
                            });

                        } else if (itemId.startsWith("@")) {
                            searchDialog.progressBar.setVisibility(View.GONE);
                            searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                            searchDialog.searchEdit.setText("");
                            searchDialog.dismiss();
                            if (order.getSwitch_id() != null) {
                                if (order.getSwitch_auto()) {
                                    switchUser(order, app, getOrderId);
                                } else {
                                    logoutDialog1 = new HintDialog(getActivity(), "提示", order.getTitle(), "立即切换", new HintDialog.OnCustomDialogListener() {
                                        @Override
                                        public void back(String query) {
                                            if ("confirm".equals(query)) {
                                                logoutDialog1.progressBar.setVisibility(View.VISIBLE);
                                                logoutDialog1.comfirmBt.setVisibility(View.GONE);
                                                switchUser(order, app, getOrderId);
                                            } else {
                                                logoutDialog1.progressBar.setVisibility(View.GONE);
                                                logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                                            }
                                            logoutDialog1.dismiss();
                                        }
                                    });
                                    logoutDialog1.setTitle("提示");
                                    logoutDialog1.show();
                                }
                            } else if (order.getMandatory()) {
                                AlertDialog dialog = new AlertDialog.Builder(VerifyFragment.this.getContext())
                                        .setTitle("提示").setMessage(order.getTitle())
                                        .setPositiveButton("关闭", null)
                                        .create();
                                dialog.show();
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                            } else {
                                Alerter.create(getActivity())
                                        .setText(order.getTitle())
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            }
                        } else {
                            searchDialog.progressBar.setVisibility(View.GONE);
                            searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                            searchDialog.dismiss();
                            Alerter.create(getActivity())
                                    .setText("请输入正确的订单号或代码")
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                    } else {
                        if (order.getErrorCode() != null) {
                            String errorCode = order.getErrorCode();
                            if ("401".equals(errorCode)) {
                                logoutDialog2 = new HintDialog(getActivity(), "提示", "登录过期，是否重新登录", "登  录", new HintDialog.OnCustomDialogListener() {
                                    @Override
                                    public void back(String query) {
                                        if (query.equals("confirm")) {
                                            app.setChecked("false");
                                            app.setJump("false");
                                            logoutDialog2.progressBar.setVisibility(View.VISIBLE);
                                            logoutDialog2.comfirmBt.setVisibility(View.GONE);
                                            LoginOutTools.loginPastDue(app, getActivity(), app.getUserId(), app.getLoginName());
                                        } else {
                                            logoutDialog2.progressBar.setVisibility(View.GONE);
                                            logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                                        }
                                        logoutDialog2.dismiss();
                                    }
                                });
                                logoutDialog2.setTitle("提示");
                                logoutDialog2.show();
                            } else {
                                Alerter.create(getActivity())
                                        .setText(order.getErrorMessage())
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            }
                        } else {
                            Alerter.create(getActivity())
                                    .setText("网络错误")
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                        searchDialog.progressBar.setVisibility(View.GONE);
                        searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                        searchDialog.dismiss();
                    }
                }
                if (logoutDialog1 != null) {
                    logoutDialog1.progressBar.setVisibility(View.GONE);
                    logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (logoutDialog1 != null) {
                    logoutDialog1.progressBar.setVisibility(View.GONE);
                    logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                }
                searchDialog.progressBar.setVisibility(View.GONE);
                searchDialog.comfirmBt.setVisibility(View.VISIBLE);
                searchDialog.dismiss();
                if (getActivity() != null) {
                    Alerter.create(getActivity())
                            .setText(app.getErrorMessage(error))
                            .setBackgroundColorRes(R.color.alerter_alert)
                            .show();
                }
            }
        });
    }

    private void switchUser(Order order, App app, String getOrderId) {
        for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
            if (order.getSwitch_id() != null && order.getSwitch_id().equals(switchUserBeanArrayList.get(i).getId())) {
                app.setLoginName(switchUserBeanArrayList.get(i).getLoginName());
                app.setUserId(switchUserBeanArrayList.get(i).getId());
                String title = switchUserBeanArrayList.get(i).getTitle();
                if (title.indexOf("-") > 0) {
                    String[] s1 = title.split("-");
                    app.setTitle(s1[0]);
                } else {
                    app.setTitle(title);
                }
                app.setToken(switchUserBeanArrayList.get(i).getToken());
                if (payList != null) {
                    payList.clear();
                }
                if (waitList != null) {
                    waitList.clear();
                }
                if (cancelList != null) {
                    cancelList.clear();
                }
                checkLogin(switchUserBeanArrayList.get(i).getToken());
            }
        }
        getOrderItemData(app.getToken(), getOrderId);
    }

    private void checkLogin(String token) {
        final App app = (App) getActivity().getApplication();
        app.getApiService().checkLogin(token, new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 200) {
                        if (resultMessage.getResult().equals("ok")) {
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            app.setAllowPay(resultMessage.getAllow_pay());
                            String sowList = resultMessage.getAllow_show_list();
                            if (app.getShowList().equals(sowList)) {
                                Intent intent = new Intent();
                                intent.setAction("callRefresh");
                                intent.putExtra("refresh", "true");
                                intent.putExtra("tab3refresh", "true");
                                getActivity().sendBroadcast(intent);
                                app.setJump("false");
                            } else {
                                app.setShowList(sowList);
                                if (sowList.equals("true")) {
                                    Intent intent = new Intent();
                                    intent.setAction("callRefresh");
                                    intent.putExtra("refresh", "false");
                                    intent.putExtra("tab3refresh", "true");
                                    getActivity().sendBroadcast(intent);
                                    app.setJump("showList");
                                } else {
                                    Intent intent = new Intent();
                                    intent.setAction("callRefresh");
                                    intent.putExtra("refresh", "false");
                                    intent.putExtra("tab3refresh", "true");
                                    getActivity().sendBroadcast(intent);
                                    app.setJump("simple");
                                }
                            }

                        } else {
                            app.setJump("false");
                        }
                    } else {
                        app.setJump("false");
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Alerter.create(getActivity())
                        .setText("网络不给力")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    private JSONArray json = new JSONArray();
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();

    private void stringToJsonArray() {
        switchUserBeanArrayList = switchUserList();
        for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
            json.put(switchUserBeanArrayList.get(i).getToken());
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        ArrayList<SwitchUserBean> cameraList = new ArrayList<>();
        SwitchUserSql cameraSql = new SwitchUserSql(getActivity());
        cameraList = cameraSql.queryDataToSQLite();
        return cameraList;
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

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            VerifyFragment.this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                    1);
        } else {
            Intent intent = new Intent(getActivity(), ScannerActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivity(intent);
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private String jump, refresh;

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("jump") != null) {
                jump = intent.getExtras().getString("jump");
                if (jump != null) {
                    if (jump.equals("showList")) {
                        Intent intent1 = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent1);
                        getActivity().finish();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiverLive);
    }
}
