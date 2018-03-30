package com.wetoop.storeoperator.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/4/8.
 */

public class SearchDialog extends Dialog {
    private SearchDialog.OnCustomDialogListener customDialogListener;
    private String searchEditStr;
    private Context context;
    public RelativeLayout progressBar;//取消按钮
    public Button comfirmBt;//确定按钮
    public AutoCompleteTextView searchEdit;
    private int lastLength = 0;//记录前一次字符串长度

    public SearchDialog(Context context,SearchDialog.OnCustomDialogListener customDialogListener) {
        super(context);
        this.context = context;
        this.customDialogListener = customDialogListener;
    }

    public interface OnCustomDialogListener {
        void back(String startTime);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_time_dialog);
        progressBar = (RelativeLayout) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        RelativeLayout cancelBt = (RelativeLayout) findViewById(R.id.cancel_dialog);
        comfirmBt = (Button) findViewById(R.id.comfirm_dialog);
        searchEdit = (AutoCompleteTextView) findViewById(R.id.searchEdit);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {//s长度等于0时setColor会报错
                    searchEdit.removeTextChangedListener(this);//解除文字改变事件绑定
                    String inputData = searchEdit.getText().toString().trim().toLowerCase();
                    searchEdit.setText(setColor(context, inputData));
                    if(lastLength > inputData.length()) {
                        searchEdit.setSelection(start);
                    }else if(lastLength == inputData.length()){
                        //edittext里的数据剪切之后粘贴到edittext的光标位置设置
                        searchEdit.setSelection(inputData.length());
                    }else {
                        if((lastLength == 0 && start == 0) || (inputData.length() - lastLength) > 1){
                            //直接粘贴进edittext时的光标位置设置
                            searchEdit.setSelection(inputData.length());
                        }else
                            searchEdit.setSelection(start+1);
                    }
                    searchEdit.addTextChangedListener(this);//重新绑定
                    lastLength = inputData.length();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchEdit.requestFocus();
                InputMethodManager im = (InputMethodManager) searchEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                im.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 200);
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchDialog.this.dismiss();
            }
        });
        comfirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditStr = searchEdit.getText().toString();
                if (TextUtils.isEmpty(searchEditStr)) {
                    searchEdit.setError("订单号或代码不能为空");
                }else{
                    customDialogListener.back(searchEditStr);
                }
            }
        });
    }

    private boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
    private CharSequence setColor(Context context,String text) {
        SpannableStringBuilder style = new SpannableStringBuilder(text);
        for(int i=0;i<text.length();i++) {
            if (isNumeric(String.valueOf(text.charAt(i)))) {
                style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.search_num)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else{
                style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.search_letter)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return style;
    }
}
