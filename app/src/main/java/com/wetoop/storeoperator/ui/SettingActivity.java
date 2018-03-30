package com.wetoop.storeoperator.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;

/**
 * Created by Administrator on 2016/5/4.
 */
public class SettingActivity extends Activity {
    private RelativeLayout num_print, back;
    private TextView num;
    private int dialogNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowStatusBarColor(SettingActivity.this,R.color.title_clicked_color);
        setContentView(R.layout.activity_setting);
        initPreferences();
        back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        App app = (App) getApplication();
        if (!app.getPrintNum().equals("")) {
            num.setText(app.getPrintNum());
            dialogNum = Integer.parseInt(app.getPrintNum());
        } else {
            num.setText("1");
        }
    }

    private void initPreferences() {
        final String[] items = {"1", "2", "3", "4", "5"};
        num_print = (RelativeLayout) findViewById(R.id.num_print);
        num = (TextView) findViewById(R.id.print_num);
        App app = (App) getApplication();
        if (!app.getPrintNum().equals("")) {
            num.setText(app.getPrintNum());
            dialogNum = Integer.parseInt(app.getPrintNum());
        } else {
            num.setText("1");
        }
        num_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingActivity.this).setTitle("选择打印张数")
                        .setSingleChoiceItems(items, dialogNum - 1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                App app = (App) getApplication();
                                app.setPrintNum(items[item]);
                                num.setText(items[item]);
                                dialogNum = Integer.parseInt(items[item]);
                                dialog.cancel();
                            }
                        }).show();//显示对话框
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return false;
    }
}
