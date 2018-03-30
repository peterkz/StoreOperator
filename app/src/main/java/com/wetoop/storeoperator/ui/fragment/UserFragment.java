package com.wetoop.storeoperator.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.MainActivity;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.ui.SimpleMainActivity;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.ui.dialog.SwitchUserDialog;

import java.util.ArrayList;

import static com.wetoop.storeoperator.ui.fragment.OrderCancelFragment.cancelList;
import static com.wetoop.storeoperator.ui.fragment.OrderPayFragment.payList;
import static com.wetoop.storeoperator.ui.fragment.OrderWaitFragment.waitList;

/**
 * Created by Administrator on 2017/4/25.
 */
public class UserFragment extends Fragment {
    private RelativeLayout addUser;
    private TextView nameText;
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private MyBroadcastReceiver broadcastReceiverLive;
    private IntentFilter intentFilterLive;
    private SwitchUserDialog switchUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_user, container, false);
        final App app = (App) getActivity().getApplication();
        nameText = (TextView) rootView.findViewById(R.id.name);
        if (app.getTotalName() != null) {
            if (app.getTotalName().indexOf("-") > 0) {
                String[] name = app.getTotalName().split("-");
                //userText = (TextView)rootView.findViewById(R.id.user);
                nameText.setText( name[1]);
            } else {
                nameText.setText("(获取失败，请重新登录进行获取)");
            }
        } else {
            nameText.setText("(获取失败，请重新登录进行获取)");
        }
        addUser = (RelativeLayout) rootView.findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchUserBeanArrayList = switchUserList();
                switchUser = new SwitchUserDialog(getActivity(),switchUserBeanArrayList, new SwitchUserDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String token, String title, String jump, String jumpEquals) {
                        if (token.equals("changeUser")) {
                            LoginOutTools.loginSimple(app,getActivity());
                        } else if (token.equals("error")) {
                            LoginOutTools.loginPastDue(app,getActivity(),switchUser.jumpId,switchUser.jumpLoginName);
                            Toast.makeText(getActivity(), "切换失败，请重新登录", Toast.LENGTH_SHORT).show();
                        } else if (token.equals("outLogin")) {
                            LoginOutTools.loginPastDue(app,getActivity(),switchUser.jumpId,switchUser.jumpLoginName);
                            Toast.makeText(getActivity(), "登录过期，请重新登录", Toast.LENGTH_SHORT).show();
                        } else {
                            App app = (App) getActivity().getApplication();
                            if (title.indexOf("-") > 0) {
                                String[] s1 = title.split("-");
                                nameText.setText(s1[1]);
                                app.setTitle(s1[0]);
                            } else {
                                nameText.setText( title);
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
                            app.setLoginName(switchUserBeanArrayList.get(Integer.parseInt(token)).getLoginName());
                            if (jump.equals("false")) {
                                Intent intent = new Intent();
                                intent.setAction("callRefresh");
                                intent.putExtra("refresh", "true");
                                getActivity().sendBroadcast(intent);
                            } else if (jump.equals("simple")) {
                                Intent intent = new Intent(getActivity(), SimpleMainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            } else if (jump.equals("showList")) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                            Toast.makeText(getActivity(), "切换成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                switchUser.show();
            }
        });

        broadcastReceiverLive = new MyBroadcastReceiver();
        intentFilterLive = new IntentFilter("callRefresh");
        getActivity().registerReceiver(broadcastReceiverLive, intentFilterLive);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiverLive);
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        ArrayList<SwitchUserBean> cameraList = new ArrayList<>();
        SwitchUserSql cameraSql = new SwitchUserSql(getActivity());
        cameraList = cameraSql.queryDataToSQLite();
        return cameraList;
    }

    private String refresh;

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("tab3refresh") != null) {
                refresh = intent.getExtras().getString("tab3refresh");
                if (refresh != null) {
                    if (refresh.equals("true")) {
                        Message msg = new Message();
                        msg.what = 1;//标志是哪个线程传数据
                        mHandler.sendMessage(msg);//发送message信息
                    }
                }
            }
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (nameText != null && getActivity() != null) {
                        App app = (App) getActivity().getApplication();
                        String title = app.getTotalName();
                        if (title.indexOf("-") > 0) {
                            String[] s1 = title.split("-");
                            nameText.setText(s1[1]);
                            app.setTitle(s1[0]);
                        } else {
                            nameText.setText(title);
                            app.setTitle(title);
                        }
                    }
                    break;
            }
        }
    };
}