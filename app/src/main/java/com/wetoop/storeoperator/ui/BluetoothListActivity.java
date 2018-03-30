package com.wetoop.storeoperator.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;

/**
 * Created by WETOOP on 2018/3/12.
 */

public class BluetoothListActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private RelativeLayout backButton;
    private TextView searchTextView;
    private Activity THIS;
    private Handler handler = new Handler();
    private App app;
    private int i = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String text = "搜索中";
            if (i == 0) {
                i++;
            } else if (i == 1) {
                text += ".";
                i++;
            } else if (i == 2) {
                text += ".. ";
                i++;
            } else {
                text += "...";
                i = 0;
            }
            searchTextView.setText(text);
            handler.postDelayed(this, 400);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        THIS = this;
        app = (App) getApplicationContext();
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.v4Support.startScan();
        handler.post(runnable);
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.list_view);
        backButton = (RelativeLayout) findViewById(R.id.back_btn);
        searchTextView = (TextView) findViewById(R.id.text_search);
    }

    private void initListener() {
        listView.setAdapter(App.v4Support.getAdapter());
        backButton.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App.v4Support.stopScan();
                if (App.v4Support.onItemClickListener != null)
                    App.v4Support.onItemClickListener.onItemClick(parent, view, position, id);
                if ("false".equals(app.getChecked())) {
                    Toast.makeText(THIS, "开始打印...", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                if ("true".equals(app.getChecked())) {
                    app.setChecked("false");
                }
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        App.v4Support.stopScan();
    }
}