package com.wetoop.storeoperator.bluetooth.v4;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.widget.AdapterView;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.api.ESCPos;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;
import com.wetoop.storeoperator.bluetooth.thread.AcceptThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectedThread;
import com.wetoop.storeoperator.bluetooth.v4.callback.WCBluetoothGattCallback;
import com.wetoop.storeoperator.ui.BluetoothListActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author WETOOP
 * @Date 2018/3/6.
 * @Description 蓝牙4.0实现
 */

public abstract class BH_V4Support extends BluetoothHandler {

    public boolean scanning;
    public BluetoothGatt bluetoothGatt;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gattCharacteristic;
    public boolean written;
    public List<BluetoothGattService> services = new ArrayList<>();
    private byte[] b2 = ESCPos.printNVImage(1, 0);
    private byte[] b3 = new byte[]{0x0a};
    private WCBluetoothGattCallback mGattCallback = new WCBluetoothGattCallback(this);
    public AdapterView.OnItemClickListener onItemClickListener;

    public BH_V4Support(Context context, Handler handler) {
        super(context, handler);
    }

    public static BH_V4Support newInstance(Context context, Handler handler) {
        BH_V4Support supportHandler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportHandler = new System_V5Support(context, handler);
        } else {
            supportHandler = new System_V4Support(context, handler);
        }
        App.v4Support = supportHandler;
        return supportHandler;
    }

    @Override
    public AcceptThread getAcceptThread() {
        return new AcceptThread(this) {
            @Override
            public void run() {
                if (connectThread != null && !connectThread.isAlive())
                    connectThread.start();
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void cancel() {
            }
        };
    }

    @Override
    public ConnectThread getConnectThread(BluetoothDevice device) {
        return new ConnectThread(this, device) {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                try {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(this.device.getAddress());
                    bluetoothGatt = device.connectGatt(context, false, mGattCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void cancel() {
            }
        };
    }

    @Override
    public ConnectedThread getConnectedThread(BluetoothSocket socket) {
        return new ConnectedThread(this, socket) {
            @Override
            public void run() {
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void write(byte[] buffer) {
                if (gattService == null || gattCharacteristic == null) {
                    if (buffer != null) {
                        byte[] data = new byte[buffer.length - (b2.length + b3.length)];
                        for (int i = 0; i < data.length; i++) {
                            data[i] = buffer[i];
                        }
                        buffer = data;
                    }

                    if (services.size() == 0) {
                        bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                        return;
                    }
                    gattService = services.get(0);
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        if (characteristicUUIDs.contains(characteristic.getUuid().toString().toLowerCase()) || characteristicUUIDs.contains(characteristic.getUuid().toString().toUpperCase())) {
                            gattCharacteristic = characteristic;
                            characteristic.setValue(buffer);
                            bluetoothGatt.writeCharacteristic(characteristic);
                            break;
                        }
                    }
                    if (gattCharacteristic == null) {
                        bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                    }
                } else {
                    ByteBuffer dataBuffer = ByteBuffer.allocate(b2.length + b3.length);
                    dataBuffer.put(b2);
                    dataBuffer.put(b3);
                    gattCharacteristic.setValue(dataBuffer.array());
                    bluetoothGatt.writeCharacteristic(gattCharacteristic);
                    written = true;
                }
            }

            @Override
            public void cancel() {
            }
        };
    }

    @Override
    public void showDeviceList(final AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
        if (adapter == null)
            adapter = getBluetoothPrinterAdapter();
        context.startActivity(new Intent(context, BluetoothListActivity.class));
    }

    public abstract void startScan();

    public abstract void stopScan();
}
