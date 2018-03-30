package com.wetoop.storeoperator.tools;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wetoop.storeoperator.R;

/**
 * @author Parck.
 * @date 2017/10/16.
 * @desc 倒计时工具类
 */
public class CountDownTool {

    private boolean running;
    private int timeTotal = 5;
    private int countDownTime = 5;
    private Button countDownButton;

    public CountDownTool(Button countDownButton) {
        init(countDownButton);
    }

    private void init(Button countDownButton) {
        this.countDownButton = countDownButton;
        //start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            post(countDownTask);
        }
    };

    private Runnable countDownTask = new Runnable() {
        @Override
        public void run() {
            if (countDownTime > 0) {
                countDownTime--;
                countDownButton.setText("登录（"+countDownTime+"）");
                countDownButton.setBackgroundResource(R.drawable.login_button_cannot_clicked);
                handler.postDelayed(this, 1000 * 1);
            } else {
                stop();
            }
        }
    };

    public void start() {
        running = true;
        countDownButton.setEnabled(false);
        handler.post(countDownTask);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(countDownTask);
        countDownButton.setEnabled(true);
        countDownButton.setBackgroundResource(R.drawable.login_button_background);
        countDownButton.setText(R.string.login_button);
        countDownTime = timeTotal;
    }

    public void finish() {
        if (running) stop();
        handler = null;
        countDownTask = null;
        countDownButton = null;
    }

}
