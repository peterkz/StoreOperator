package com.wetoop.storeoperator;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wetoop.storeoperator.api.ESCPos;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.bean.OrderBean;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/10/8.
 */
public class BluetoothService extends Service {
    BluetoothHandler bluetoothHandler;
    private Order orderItem;
    public static BluetoothDevice B_item = null;
    ArrayList<OrderBean> orderList = new ArrayList<OrderBean>();
    private String Purchased_sql = "";
    private String Purchased_service = "";
    private int countTest = 0;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //Toast.makeText(this, "开始service", Toast.LENGTH_LONG).show();
        super.onStart(intent, startId);
        if (bluetoothHandler != null) {
            bluetoothHandler.stop();
        }
        bluetoothHandler = BluetoothHandler.newInstance(this, mHandler);
        bluetoothHandler.start();
        BluetoothDevice item = intent.getParcelableExtra("buletooth");
        B_item = item;
        orderItem = intent.getParcelableExtra("orderItem");
        //B_item = intent.getParcelableExtra("buletooth");
        deviceSelected(B_item);
    }

    public void deviceSelected(BluetoothDevice item) {
        // try to connect to this device
        bluetoothHandler.connect(item);
    }

    public void print(Order orderItem) {
        String note = "";
        if (orderItem != null) {
            App app = (App) getApplication();

            String status = getString(R.string.status_created);
            String status_used = getString(R.string.status_created);

            if (orderItem.getPurchasedAt() != null) {
                if (orderItem.getUsed() != null) {
                    status_used = getString(R.string.status_used) +
                            "\n" + getString(R.string.used)+ orderItem.getUsed() +
                            "\n" + getString(R.string.purchased)  + dateFormat.format(orderItem.getPurchasedAt());
                } else {
                    status = getString(R.string.status_purchased) +
                            "\n" + getString(R.string.purchased) + dateFormat.format(orderItem.getPurchasedAt());
                }

            }

            if (orderItem.getCancelledAt() != null) {
                status = getString(R.string.status_cancelled) +
                        "\n" + getString(R.string.cancelled) + ": " + dateFormat.format(orderItem.getCancelledAt());
            }

            if (orderItem.getCustomer_note() != null) {
                note = "备注：" + orderItem.getCustomer_note();
            } else {
                note = "备注：无";
            }

            String content;
            if (orderItem.getAddress() != null) {
                if (orderItem.getUsed() != null) {
                    content = "\n" + app.getTitle() +
                            "\n" + orderItem.getId() +
                            "\n\n" + orderItem.getTitle() + "\r\n￥" + String.format("%.2f (%s %s)", orderItem.getTotalPrice(), getString(R.string.funds_pay), orderItem.getFundsAdjust()) +
                            "\n" + getString(R.string.created) + ": " + dateFormat.format(orderItem.getCreatedAt()) +
                            "\n" + status_used +
                            "\n" + "地址" + ": " + orderItem.getAddress() +
                            "\n" + note +
                            "\n";
                } else {
                    content = "\n" + app.getTitle() +
                            "\n" + orderItem.getId() +
                            "\n\n" + orderItem.getTitle() + "\r\n￥" + String.format("%.2f (%s %s)", orderItem.getTotalPrice(), getString(R.string.funds_pay), orderItem.getFundsAdjust()) +
                            "\n" + getString(R.string.created) + ": " + dateFormat.format(orderItem.getCreatedAt()) +
                            "\n" + status +
                            "\n" + "地址" + ": " + orderItem.getAddress() +
                            "\n" + note +
                            "\n";
                }

            } else {
                if (orderItem.getUsed() != null) {
                    content = "\n" + app.getTitle() +
                            "\n" + orderItem.getId() +
                            "\n\n" + orderItem.getTitle() + "\r\n￥" + String.format("%.2f (%s %s)", orderItem.getTotalPrice(), getString(R.string.funds_pay), orderItem.getFundsAdjust()) +
                            "\n" + getString(R.string.created) + ": " + dateFormat.format(orderItem.getCreatedAt()) +
                            "\n" + status_used +
                            "\n" + note +
                            "\n";
                } else {
                    content = "\n" + app.getTitle() +
                            "\n" + orderItem.getId() +
                            "\n\n" + orderItem.getTitle() + "\r\n￥" + String.format("%.2f (%s %s)", orderItem.getTotalPrice(), getString(R.string.funds_pay), orderItem.getFundsAdjust()) +
                            "\n" + getString(R.string.created) + ": " + dateFormat.format(orderItem.getCreatedAt()) +
                            "\n" + status +
                            "\n" + note +
                            "\n";
                }

            }
            byte[] b1 = ESCPos.printLine(content);
            byte[] b2 = ESCPos.printNVImage(1, 0);
            byte[] b3 = new byte[]{0x0a};

            ByteBuffer dataBuffer = ByteBuffer.allocate(b1.length + b2.length + b3.length);
            dataBuffer.put(b1);
            dataBuffer.put(b2);
            dataBuffer.put(b3);

            bluetoothHandler.write(dataBuffer.array());

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 600);
        } else {
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothHandler.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothHandler.STATE_CONNECTED:   //已连接
                            print(orderItem);
                            break;
                        case BluetoothHandler.STATE_CONNECTING:  //正在连接
                            break;
                        case BluetoothHandler.STATE_LISTEN:     //监听连接的到来
                        case BluetoothHandler.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothHandler.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    break;
                case BluetoothHandler.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    break;
            }
        }

    };

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        bluetoothHandler.stop();
        Log.v("Bluetooth", "关闭service");
    }
}
