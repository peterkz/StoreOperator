package com.wetoop.storeoperator.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.AdapterView;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bluetooth.thread.AcceptThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectedThread;
import com.wetoop.storeoperator.bluetooth.v3.BH_V3Support;
import com.wetoop.storeoperator.bluetooth.v4.BH_V4Support;
import com.wetoop.storeoperator.ui.adapter.BluetoothPrinterAdapter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @Author WETOOP
 * @Date 2018/3/6.
 * @Description 蓝牙处理器抽象类
 */

public abstract class BluetoothHandler {

    public final static String TAG = "BluetoothHandler";
    public final static boolean D = true;
    public final static int MESSAGE_STATE_CHANGE = 1;
    public final static int MESSAGE_CONNECTION_LOST = 5;
    public final static int MESSAGE_UNABLE_CONNECT = 6;
    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final static int STATE_NONE = 0;
    public final static int STATE_LISTEN = 1;
    public final static int STATE_CONNECTING = 2;
    public final static int STATE_CONNECTED = 3;

    public int mState = 0;
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public Handler handler;
    public AcceptThread acceptThread;
    public ConnectThread connectThread;
    public ConnectedThread connectedThread;

    public List<BluetoothDevice> devices = new ArrayList<>();
    protected BluetoothPrinterAdapter adapter;
    public Context context;
    public Set<String> serviceUUIDs = new HashSet<>();
    public Set<String> characteristicUUIDs = new HashSet<>();

    public BluetoothHandler(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public static BluetoothHandler newInstance(Context context, Handler handler) {
        BluetoothHandler bluetoothHandler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothHandler = BH_V4Support.newInstance(context, handler);
        } else {
            bluetoothHandler = new BH_V3Support(context, handler);
        }
        bluetoothHandler.init();
        return bluetoothHandler;
    }

    public void init() {
        if (App.userInfo == null) {
            final App app = (App) context.getApplicationContext();
            app.getApiService().checkLogin(app.getToken(), new Callback<UserInfo>() {
                @Override
                public void success(UserInfo userInfo, Response response) {
                    if (userInfo != null && userInfo.getPrinter_service() != null) {
                        App.userInfo = userInfo;
                        for (String s : userInfo.getPrinter_service()) {
                            String[] split = s.split("\\|");
                            if (split.length < 3 || !"1".equals(split[0])) continue;
                            serviceUUIDs.add(split[1]);
                            characteristicUUIDs.add(split[2]);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            for (String s : App.userInfo.getPrinter_service()) {
                String[] split = s.split("\\|");
                if (split.length < 3 || !"1".equals(split[0])) continue;
                serviceUUIDs.add(split[1]);
                characteristicUUIDs.add(split[2]);
            }
        }
    }

    //开始与目标蓝牙设备连接
    public synchronized void connect(BluetoothDevice device) {
        if (this.mState == 2 && this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
        this.connectThread = getConnectThread(device);
        this.connectThread.start();
        this.setState(2);
    }

    public synchronized void connected(BluetoothSocket socket) {
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
        if (this.acceptThread != null) {
            this.acceptThread.cancel();
            this.acceptThread = null;
        }
        this.connectedThread = getConnectedThread(socket);
        this.connectedThread.start();
        Message msg = this.handler.obtainMessage(4);
        this.handler.sendMessage(msg);
        this.setState(3);
    }


    //蓝牙开始启动
    public synchronized void start() {
        if (this.bluetoothAdapter == null) {
            return;
        }
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
        if (this.acceptThread == null) {
            this.acceptThread = getAcceptThread();
            this.acceptThread.start();
        }
        this.setState(1);
    }

    //蓝牙停止工作
    public synchronized void stop() {
        this.setState(0);
        if (this.connectThread != null) {
            this.connectThread.cancel();
            this.connectThread = null;
        }
        if (this.connectedThread != null) {
            this.connectedThread.cancel();
            this.connectedThread = null;
        }
        if (this.acceptThread != null) {
            this.acceptThread.cancel();
            this.acceptThread = null;
        }
    }

    protected synchronized boolean isAvailable() {
        return bluetoothAdapter != null;
    }

    protected synchronized boolean isEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    protected synchronized BluetoothDevice getDeviceByMac(String mac) {
        return bluetoothAdapter.getRemoteDevice(mac);
    }

    public synchronized BluetoothDevice getDeviceByName(String name) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains(name)) {
                return device;
            }
        }
        return null;
    }

    public synchronized void sendMessage(String message, String charset) {
        if (message.length() > 0) {
            byte[] send;
            try {
                send = message.getBytes(charset);
            } catch (UnsupportedEncodingException var5) {
                send = message.getBytes();
            }
            this.write(send);
            byte[] tail = new byte[]{(byte) 10, (byte) 13, (byte) 0};
            this.write(tail);
        }
    }

    //往目标蓝牙设备写入数据
    public void write(byte[] out) {
        ConnectedThread thread;
        synchronized (this) {
            if (this.mState != 3) {
                return;
            }
            thread = this.connectedThread;
        }
        thread.write(out);
    }

    public synchronized void setState(int state) {
        this.mState = state;
        this.handler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }


    public void connectionFailed() {
        this.setState(1);
        Message msg = this.handler.obtainMessage(6);
        this.handler.sendMessage(msg);
    }

    public void connectionLost() {
        Message msg = this.handler.obtainMessage(5);
        this.handler.sendMessage(msg);
    }

    public abstract AcceptThread getAcceptThread();

    public abstract ConnectThread getConnectThread(BluetoothDevice device);

    public abstract ConnectedThread getConnectedThread(BluetoothSocket socket);

    public BluetoothPrinterAdapter getBluetoothPrinterAdapter() {
        this.adapter = new BluetoothPrinterAdapter(context, devices);
        return adapter;
    }

    public abstract void showDeviceList(AdapterView.OnItemClickListener listener);

    public BluetoothPrinterAdapter getAdapter() {
        return this.adapter;
    }
}
