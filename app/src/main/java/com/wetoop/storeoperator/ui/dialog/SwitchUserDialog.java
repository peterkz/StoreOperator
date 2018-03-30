package com.wetoop.storeoperator.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.LoginActivity;
import com.wetoop.storeoperator.ui.ScannerActivity;
import com.wetoop.storeoperator.ui.adapter.SpinnerSwitchAdapter;
import com.wetoop.storeoperator.ui.adapter.SwitchUserAdapter;
import com.wetoop.storeoperator.ui.widget.SpinnerSwitchUser;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Administrator on 2017/3/23.
 */
public class SwitchUserDialog extends Dialog implements SpinnerSwitchAdapter.IOnItemSwitchSelectListener{
    private OnCustomDialogListener customDialogListener;
    private LayoutInflater inflater;
    public String jumpId;
    public String jumpLoginName;
    private Activity context;
    private RelativeLayout back;
    private RelativeLayout addUser;
    private ListView listView;
    private TextView menuBackground;
    private SpinnerSwitchUser switchUserSpinner;
    private SpinnerSwitchAdapter switchAdapter;
    private HintDialog hintDialog;
    private int choisePos = 0;
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private List<String> items = new ArrayList<>();

    public SwitchUserDialog(Activity context, ArrayList<SwitchUserBean> switchUserBeanArrayList, OnCustomDialogListener customDialogListener) {
        super(context);
        this.context = context;
        this.customDialogListener = customDialogListener;
        this.switchUserBeanArrayList = switchUserBeanArrayList;
    }

    public interface OnCustomDialogListener {
        void back(String token, String title, String jump, String jumpEquals);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        inflater = (context).getLayoutInflater();
        View localView = inflater.inflate(R.layout.switch_dialog, null);
        localView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_bottom_to_top));
        setContentView(localView);
        getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        //设置标题
        //setTitle(name);
        listView = (ListView) findViewById(R.id.switch_user_dialog_list_view);
        SwitchUserAdapter myadapter = new SwitchUserAdapter(context, switchUserBeanArrayList);
        listView.setAdapter(myadapter);
        back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchUserDialog.this.dismiss();
            }
        });
        menuBackground = (TextView) findViewById(R.id.menuBackground);
        addUser = (RelativeLayout) findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialogListener.back("changeUser", "", "", "");
                SwitchUserDialog.this.dismiss();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeUser(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                choisePos = position;
                //Log.e("onItemLongClick", "onItemLongClick: "+choisePos);
                menuBackground.setVisibility(View.VISIBLE);
                switchUserSpinner.showAsDropDown(view);
                return true;
            }
        });
        popItemList();
    }

    private void popItemList() {
        items.add("删除");
        switchAdapter = new SpinnerSwitchAdapter(context, items);
        switchUserSpinner = new SpinnerSwitchUser(context);
        switchUserSpinner.setSwitchAdapter(switchAdapter);
        switchUserSpinner.setItemSwitchListener(this);
        switchUserSpinner.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                menuBackground.setVisibility(View.GONE);//菜单收回时隐藏背景
            }
        });

    }

    @Override
    public void onItemSwitchClick(int pos) {
        switch (pos){
            case 0:
                String[] name = switchUserBeanArrayList.get(choisePos).getTitle().split("-");
                hintDialog = new HintDialog(context, "提示", "您确定要将帐户“" + name[1] + "”从帐号列表删除吗？", "删除", new HintDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String query) {
                        if (query.equals("confirm")) {
                            App app = (App) context.getApplicationContext();
                            LoginOutTools.deleteUser(context, switchUserBeanArrayList.get(choisePos).getId());
                            if(!switchUserBeanArrayList.get(choisePos).getId().equals(app.getUserId())) {
                                switchUserBeanArrayList.clear();
                                switchUserBeanArrayList = switchUserList();
                                if (switchUserBeanArrayList.size() > 0) {
                                    SwitchUserAdapter myAdapter = new SwitchUserAdapter(context, switchUserBeanArrayList);
                                    listView.setAdapter(myAdapter);
                                } else {
                                    Toast.makeText(context, "已无可登录帐号，请重新登录", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    intent.putExtra("exit", "come");
                                    context.startActivity(intent);
                                    app.setChecked("false");
                                    app.setToken("");
                                    context.finish();
                                }
                            }else{
                                switchUserBeanArrayList.clear();
                                switchUserBeanArrayList = switchUserList();
                                if (switchUserBeanArrayList.size() > 0) {
                                    SwitchUserAdapter myAdapter = new SwitchUserAdapter(context, switchUserBeanArrayList);
                                    listView.setAdapter(myAdapter);
                                    changeUser(0);
                                } else {
                                    Toast.makeText(context, "已无可登录帐号，请重新登录", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, LoginActivity.class);
                                    intent.putExtra("exit", "come");
                                    context.startActivity(intent);
                                    app.setChecked("false");
                                    app.setToken("");
                                    context.finish();
                                }
                            }
                        } else {
                            hintDialog.progressBar.setVisibility(View.GONE);
                            hintDialog.comfirmBt.setVisibility(View.VISIBLE);
                        }
                        hintDialog.dismiss();
                    }
                });
                hintDialog.setTitle("提示");
                hintDialog.show();
                break;
        }
    }

    private void changeUser(final int position){
        final App app = (App) context.getApplicationContext();
        app.getApiService().checkLogin(switchUserBeanArrayList.get(position).getToken(), new Callback<UserInfo>() {

            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    jumpId = switchUserBeanArrayList.get(position).getId();
                    jumpLoginName = switchUserBeanArrayList.get(position).getLoginName();
                    if (resultMessage.getErrorCode() == 200) {
                        if (resultMessage.getResult().equals("ok")) {
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            app.setUserId(switchUserBeanArrayList.get(position).getId());
                            app.setAllowPay(resultMessage.getAllow_pay());
                            String sowList = resultMessage.getAllow_show_list();
                            String jump = "false";
                            String jumpEquals = "false";
                            if (app.getShowList().equals(sowList)) {
                                jump = "false";
                                if (sowList.equals("true")) {
                                    jumpEquals = "showList";
                                } else {
                                    jumpEquals = "simple";
                                }
                            } else {
                                app.setShowList(resultMessage.getAllow_show_list());
                                if (sowList.equals("true")) {
                                    jump = "showList";
                                } else {
                                    jump = "simple";
                                }
                            }
                            customDialogListener.back(String.valueOf(position), switchUserBeanArrayList.get(position).getTitle(), jump, jumpEquals);
                            SwitchUserDialog.this.dismiss();
                        }
                    } else if (resultMessage.getErrorCode() == 401) {
                        app.setUserId("outLogin");
                        customDialogListener.back("outLogin", "", "", "");
                        SwitchUserDialog.this.dismiss();
                    }
                }
                SwitchUserDialog.this.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                customDialogListener.back("error", "", "", "");
                SwitchUserDialog.this.dismiss();
            }
        });
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(context);
        return cameraSql.queryDataToSQLite();
    }
}