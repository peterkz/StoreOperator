package com.wetoop.storeoperator;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/5/4.
 */
public class ScheduledExecutorService extends Service {
    private Handler handler = new Handler();
    private String take = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        App app = (App) getApplication();
        if (intent != null) {
            if (intent.getStringExtra("take") != null) {
                if (!intent.getStringExtra("take").equals("")) {
                    take = intent.getStringExtra("take");
                }
            }
        }
        if (app.getChecked().equals("false") || take.equals("stop")) {
            app.setChecked("false");
            BluetoothDevice B_item;
            handler.removeCallbacks(runnable); //停止Timer
            B_item = null;
            stopService(B_item);
        } else {
            PowerManager pm = (PowerManager) ScheduledExecutorService.this.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();
            handler.postDelayed(runnable, 1000 * 1);// 开始Timer
            wl.release();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopService(BluetoothDevice B_item) {
        Intent intent = new Intent(ScheduledExecutorService.this, PrintService.class);
        intent.putExtra("orderItem", B_item);
        intent.setPackage(getPackageName());
        ScheduledExecutorService.this.stopService(intent);
    }

    private Runnable runnable = new Runnable() {


        public void run() {

            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter != null && mAdapter.isEnabled()) {
                Intent i = new Intent(ScheduledExecutorService.this, PrintService.class);
                i.putExtra("card", "coming");
                ScheduledExecutorService.this.startService(i);

                handler.postDelayed(this, 1000 * 10);     //postDelayed(this,1000)方法安排一个Runnable对象到主线程队列中
            } else {
                if (mAdapter != null) {
                    Toast.makeText(ScheduledExecutorService.this, "正在打开蓝牙", Toast.LENGTH_SHORT).show();
                    mAdapter.enable();
                    Intent i = new Intent(ScheduledExecutorService.this, PrintService.class);
                    i.putExtra("card", "coming");
                    ScheduledExecutorService.this.startService(i);

                    handler.postDelayed(this, 1000 * 10);     //postDelayed(this,1000)方法安排一个Runnable对象到主线程队列中
                }
            }

        }


    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
