package com.wetoop.storeoperator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.wetoop.storeoperator.api.ESCPos;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.bean.OrderBean;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;
import com.wetoop.storeoperator.factory.PriorityExecutor;
import com.wetoop.storeoperator.sql.OrderSql;
import com.wetoop.storeoperator.ui.MainActivity;
import com.wetoop.storeoperator.ui.adapter.BluetoothPrinterAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Administrator on 2015/9/23.
 */
public class PrintService extends Service {
    private static final String TAG = "ServiceDemo";
    public static final String ACTION = "com.wetoop.storeoperator.PrintService";
    private ArrayList<Order> list_order;
    private String Purchased_sql = "";
    private String Purchased_service = "";
    private Order orderItem;
    private BluetoothHandler bluetoothHandler;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    ArrayList<OrderBean> orderList = new ArrayList<OrderBean>();
    public static BluetoothDevice B_item = null;
    public static int Enable = 0;
    NotificationManager mNotificationManager;
    Notification notification;
    private int countTest;
    PowerManager.WakeLock sCpuWakeLock;
    private Order order_Item;
    private String order_details;
    private int count_unable_connect;
    private Date created;
    private Date Purchased_created;
    private ArrayList<Order> newOrderList = new ArrayList<Order>();
    private MyReceiver receiver = new MyReceiver();
    Context context;
    private BluetoothPrinterAdapter adapter;

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "PrintService onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "PrintService onCreate");
        super.onCreate();
    }

    public void deviceSelected(BluetoothDevice item) {
        bluetoothHandler.connect(item);

    }

    public void print(Order orderItem) {
        App app = (App) PrintService.this.getApplication();
        String note = "";
        if (orderItem != null) {

            String status = getString(R.string.status_created);
            if (orderItem.getPurchasedAt() != null) {
                status = getString(R.string.status_purchased) +
                        "\n" + getString(R.string.purchased)+ dateFormat.format(orderItem.getPurchasedAt());
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
                content = "\n" + app.getTitle() +
                        "\n" + orderItem.getId() +
                        "\n\n" + orderItem.getTitle() + "\r\n￥" + String.format("%.2f (%s %s)", orderItem.getTotalPrice(), getString(R.string.funds_pay), orderItem.getFundsAdjust()) +
                        "\n" + getString(R.string.created) + ": " + dateFormat.format(orderItem.getCreatedAt()) +
                        "\n" + status +
                        "\n" + "地址" + ": " + orderItem.getAddress() +
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

            byte[] b1 = ESCPos.printLine(content);
            byte[] b2 = ESCPos.printNVImage(1, 0);

            Log.e("bi", b1.toString() + "");

            ByteBuffer dataBuffer = ByteBuffer.allocate(b1.length + b2.length + 1024);
            dataBuffer.put(b1);
            dataBuffer.put(b2);
            dataBuffer.put(new byte[]{0x0a});

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
                            if (order_details == "print") {
                                order_details = null;
                                App app = (App) getApplication();
                                if (!app.getPrintNum().equals("")) {
                                    int num = Integer.parseInt(app.getPrintNum());//需要打印的张数
                                    for (int i = 0; i < num; i++) {
                                        print(order_Item);
                                    }
                                } else {
                                    print(order_Item);
                                }
                            }
                            contrast();
                            break;
                        case BluetoothHandler.STATE_CONNECTING:  //正在连接
                            break;
                        case BluetoothHandler.STATE_LISTEN:     //监听连接的到来
                            break;
                        case BluetoothHandler.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothHandler.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    break;
                case BluetoothHandler.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    if (count_unable_connect == 0) {
                        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mAdapter != null && mAdapter.isEnabled()) {
                            mAdapter.disable();
                            mAdapter.cancelDiscovery();
                        }
                        Log.e("unable", "无法连接设备1");
                        count_unable_connect++;
                        Reconnection();
                    } else {
                        App app = (App) getApplication();
                        app.setItem(null);
                        Log.e("unable", "无法连接设备2");
                        Intent intent = new Intent(PrintService.this, ScheduledExecutorService.class);
                        intent.putExtra("take", "stop");
                        PrintService.this.startService(intent);
                        Toast.makeText(PrintService.this, "无法连接设备", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 23:
                    Log.e(TAG, "msg=23");
                    Log.e(TAG, "B_item=" + B_item);
                    if (B_item != null) {
                        deviceSelected(B_item);
                    } else {
                    }
                    break;
                case 24:
                    setDateThread();
                    break;
                case 25:
                    countTest = 0;
                    break;
                case 26:
                    test();
                    break;
            }
        }

    };

    public void contrast() {
        Log.e(TAG, "查找打印");
        App app = (App) PrintService.this.getApplication();
        if (!app.getPrintNum().equals("")) {
            int num = Integer.parseInt(app.getPrintNum());
            for (int i = 0; i < newOrderList.size(); i++) {
                for (int j = 0; j < num; j++) {
                    print(newOrderList.get(i));
                }
            }
            newOrderList.clear();
        } else {
            for (int i = 0; i < newOrderList.size(); i++) {
                print(newOrderList.get(i));
            }
            newOrderList.clear();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager pm = (PowerManager) PrintService.this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        App app = (App) getApplication();
        count_unable_connect = 0;
        B_item = app.getItem();
        if (B_item != null && app.getChecked().equals("true")) {
            ShowNotification();
        }
        Log.e(TAG, "" + B_item + "位置" + app.getPos());
        if (B_item == null && app.getPos() != 100) {
            Reconnection();
        }
        blue_listener_thread();
        printData();
        wl.release();
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void Reconnection() {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null && !mAdapter.isEnabled()) {
            mAdapter.enable();
        }
        App app = (App) getApplication();
        app.setReconnection("false");
        PriorityExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                App app = (App) getApplication();
                while (app.getReconnection().equals("false")) {
                    BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (mAdapter != null && mAdapter.isEnabled()) {
                        Log.e(TAG, "正在重新连接蓝牙设备···");
                        bluetoothHandler = BluetoothHandler.newInstance(PrintService.this, mHandler);
                        bluetoothHandler.start();
                        if (adapter == null)
                            adapter = bluetoothHandler.getBluetoothPrinterAdapter();
                        try {
                            Thread.sleep(2 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        B_item = adapter.getItem(app.getPos());
                        app.setItem(B_item);
                        app.setReconnection("true");
                    }
                }
            }
        });
    }

    void blue_listener_thread() {
        App app = (App) getApplication();
        checked_n = app.getChecked();
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null && !mAdapter.isEnabled()) {
            Reconnection();
        } else {
            Log.e(TAG, "蓝牙已连接");
        }
    }

    String checked_n;
    WifiManager.WifiLock wifiLock = null;

    void printData() {
        PowerManager pm = (PowerManager) PrintService.this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        checked_n = ((App) getApplication()).getChecked();
        if (wifiLock == null) {
            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiLock = manager.createWifiLock("SwiFTP");
            wifiLock.setReferenceCounted(false);
        }
        wifiLock.acquire();

        Log.e(TAG, "PrintService onStart");
        Message msg = new Message();
        msg.what = 24;//标志是哪个线程传数据
        mHandler.sendMessage(msg);//发送message信息

        IntentFilter filter = new IntentFilter();
        filter.addAction("print");
        registerReceiver(receiver, filter);
        if ("print".equals(order_details)) {
            Log.e(TAG, "order_details");
            if (bluetoothHandler != null) {
                bluetoothHandler.stop();
            }
            bluetoothHandler = BluetoothHandler.newInstance(PrintService.this, mHandler);
            bluetoothHandler.start();

            msg = new Message();
            msg.what = 23;//标志是哪个线程传数据
            mHandler.sendMessage(msg);//发送message信息
        }

        if (countTest == 1) {
            Log.e(TAG, "进入countText1");

            if (bluetoothHandler != null) {
                bluetoothHandler.stop();
            }
            bluetoothHandler = BluetoothHandler.newInstance(PrintService.this, mHandler);
            bluetoothHandler.start();

            msg = new Message();
            msg.what = 23;//标志是哪个线程传数据
            mHandler.sendMessage(msg);//发送message信息
        }

        if (wifiLock != null && checked_n == "false") {
            wifiLock.release();
        }
        wl.release();
    }

    public void ShowNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        //定义通知栏展现的内容信息
        CharSequence tickerText = "自动打印已开启";
        CharSequence contentTitle = "微查单";
        CharSequence contentText = "自动打印已开启";
        long when = System.currentTimeMillis();

        Intent intent = new Intent(PrintService.this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra("checked_N", "checked");
        //使用这个，设置返回到MainActivity时可以不用再启动一个新的MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(PrintService.this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(tickerText)
                .setWhen(when)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
        context = getApplicationContext();

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
        mNotificationManager.notify(0, notification);
    }

    private long lastModified = 0;

    public void setDateThread() {
        PriorityExecutor.getInstance().execute(new Runnable() {
            public void run() {
                InputStream is = null;
                try {
                    App app = (App) getApplication();
                    lastModified = app.getLastModifiedService();
                    URL url = new URL("https://wx.wetoop.com/service/store-app/orders/" + 0);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.addRequestProperty("SA-Token", app.getToken());
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    if (lastModified > 0) {
                        conn.setIfModifiedSince(lastModified);
                    }
                    conn.setDoInput(true);
                    long lastModifiedTest = conn.getLastModified();

                    // Starts the query
                    conn.connect();

                    if (lastModifiedTest == 0) {
                        //app.setLastModified(lastModified);
                        int code = conn.getResponseCode();
                        if (code == 304) {
                            Message msg = new Message();
                            msg.what = 25;//标志是哪个线程传数据
                            mHandler.sendMessage(msg);//发送message信息
                        }
                    } else {
                        app.setLastModifiedService(lastModifiedTest);
                        Message msg = new Message();
                        msg.what = 26;//标志是哪个线程传数据
                        mHandler.sendMessage(msg);//发送message信息
                    }

                } catch (IOException e) {
                    // Nothing
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // Nothing
                        }
                    }
                }
            }
        });
    }

    //检测是否有新的数据
    public void test() {
        Log.e(TAG, "检测新数据进入test");
        App app = (App) PrintService.this.getApplication();
        //获取数据
        app.getApiService().orderList(app.getToken(), 0, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    Toast.makeText(PrintService.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                countTest = 0;
                int count = 0;
                for (int i = 0; i < orders.size(); i++) {
                    Order item = orders.get(i);
                    Purchased_service = item.getId();
                    Purchased_created = item.getPurchasedAt();
                    ArrayList<OrderBean> sql_list = new ArrayList<OrderBean>();
                    OrderSql os = new OrderSql(PrintService.this);
                    sql_list = os.getPurchasedId();
                    if (sql_list.size() == 0) {
                        Log.e(TAG, "有新数据");
                        countTest = 1;
                    } else {
                        for (int j = 0; j < sql_list.size(); j++) {
                            //Purchased_sql = sql_list.get(j).getCreatedAt();
                            Purchased_sql = sql_list.get(j).getId();
                            if (Purchased_sql != null) {
                                if (Purchased_service.equals(Purchased_sql)) {
                                    count = 5;
                                    created = Purchased_created;
                                }
                            }

                        }
                    }
                }
                newOrderList.clear();
                if (count == 5) {
                    Date dateCreated;
                    int dateCreatedPos = 0;
                    dateCreated = created;
                    for (int j = 0; j < orders.size(); j++) {
                        Order item1 = orders.get(j);
                        Date date = item1.getPurchasedAt();
                        if (date.after(created)) {
                            //dateCreated=date;
                            if (date.after(dateCreated)) {
                                dateCreated = date;
                                dateCreatedPos = j;
                            }
                            newOrderList.add(item1);
                            countTest = 1;
                        }
                    }
                    Order orderItem = orders.get(dateCreatedPos);
                    ArrayList<OrderBean> orderList = new ArrayList<OrderBean>();
                    orderList.clear();
                    OrderBean orderBean = new OrderBean();
                    orderBean.setId(orderItem.getId());
                    orderBean.setTitle(orderItem.getTitle());
                    orderBean.setTotalPrice(orderItem.getTotalPrice());
                    orderBean.setFundsAdjust(orderItem.getFundsAdjust());
                    orderBean.setCreatedAt(orderItem.getCreatedAt());
                    orderBean.setPurchasedAt(orderItem.getPurchasedAt());
                    orderBean.setCancelledAt(orderItem.getCancelledAt());
                    orderBean.setMobile(orderItem.getMobile());
                    orderList.add(orderBean);
                    OrderSql address = new OrderSql(getApplicationContext());
                    address.createTable();
                    address.setDataToSQLite(orderList);//对地址数据信息进行更新
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (PrintService.this != null) {
                    onDestroy();
                }
            }
        });

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                order_details = intent.getAction();
                order_Item = intent.getParcelableExtra("orderItem");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
