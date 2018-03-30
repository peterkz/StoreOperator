package com.wetoop.storeoperator.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.utils.PermissionUtil;

import java.math.BigDecimal;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;

/**
 * Created by Administrator on 2016/3/21.
 */
public class AllowPayActivity extends Activity {
    private String payStr;
    private EditText pay;
    private ImageView clear;
    private static String MAXNUM = "100000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allow_pay);
        setWindowStatusBarColor(AllowPayActivity.this, R.color.title_clicked_color);
        pay = (EditText) findViewById(R.id.allow_pay_edit);
        pay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    clear.setVisibility(View.VISIBLE);
                    String text = s.toString();

                    //限制开头有0时只能输入“.”
                    int posZero = text.indexOf("0");
                    if(posZero == 0){
                        if(text.length() > 1) {
                            //等于-1时是“0.”，等于2时是“0.0”
                            if (text.indexOf("0", text.indexOf(".") + 1) == -1||text.indexOf("0", text.indexOf(".") + 1) == 2) {
                            }else{
                                pay.removeTextChangedListener(this);
                                pay.setText(text.substring(0, text.length() - 1));
                                pay.setSelection(pay.getText().toString().length());
                                pay.addTextChangedListener(this);//重新绑定
                            }
                        }
                    }

                    int posDot = text.indexOf(".");
                    //小数点之前保留5位数字或者10W
                    if (posDot < 0){
                        //temp
                        if(text.equals(MAXNUM)){
                            return;
                        }else{
                            if(text.length()<=5){
                                return;
                            }else{
                                s.delete(5, 6);
                                return;
                            }
                        }
                    }else if (posDot == 0){//限制第一位不能为"."
                        pay.removeTextChangedListener(this);
                        pay.setText(text.substring(0,text.length()-1));
                        pay.setSelection(pay.getText().toString().length());
                        pay.addTextChangedListener(this);//重新绑定
                    }else{
                        //限制只能输入一个“.”
                        if(text.indexOf(".",text.indexOf(".")+1)>0){
                            pay.removeTextChangedListener(this);
                            pay.setText(text.substring(0,text.length()-1));
                            pay.setSelection(pay.getText().toString().length());
                            pay.addTextChangedListener(this);//重新绑定
                        }
                    }
                    //保留三位小数
                    if (text.length() - posDot - 1 > 2)
                        s.delete(posDot + 3, posDot + 4);
                } else {
                    clear.setVisibility(View.GONE);
                }
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pay.requestFocus();
                InputMethodManager im = (InputMethodManager) pay.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                im.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 200);
        clear = (ImageView) findViewById(R.id.del_image);
        clear.setVisibility(View.GONE);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay.setText("");
            }
        });
        RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button button = (Button) findViewById(R.id.allow_pay_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(pay.getText().toString())) {
                    BigDecimal decimal = new BigDecimal(pay.getText().toString());
                    if (decimal.compareTo(new BigDecimal(0.009999)) == 1) {
                        payStr = pay.getText().toString();
                        if (Build.VERSION.SDK_INT >= 23) {
                            showContacts();
                        } else {
                            Intent intent = new Intent(AllowPayActivity.this, ScannerActivity.class);
                            intent.putExtra("allow_coming", "coming");
                            intent.putExtra("allow_pay", payStr);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Alerter.create(AllowPayActivity.this)
                                .setText(R.string.allow_price_scope)
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                }
            }
        });
    }

    public void showContacts() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AllowPayActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            Intent intent = new Intent(AllowPayActivity.this, ScannerActivity.class);
            intent.putExtra("allow_coming", "coming");
            intent.putExtra("allow_pay", payStr);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                Intent intent = new Intent(AllowPayActivity.this, ScannerActivity.class);
                intent.putExtra("allow_coming", "coming");
                intent.putExtra("allow_pay", payStr);
                startActivity(intent);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
