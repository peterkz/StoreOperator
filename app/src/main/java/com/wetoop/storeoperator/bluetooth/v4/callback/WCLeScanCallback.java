package com.wetoop.storeoperator.bluetooth.v4.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.text.TextUtils;

import com.wetoop.storeoperator.bluetooth.v4.BH_V4Support;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WCLeScanCallback implements BluetoothAdapter.LeScanCallback {

    private BH_V4Support bluetoothHandler;

    public WCLeScanCallback(BH_V4Support bluetoothHandler) {
        this.bluetoothHandler = bluetoothHandler;
    }

    @Override
    public void onLeScan(final BluetoothDevice device, int i, byte[] bytes) {
        bluetoothHandler.handler.post(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothHandler.devices.contains(device) && !TextUtils.isEmpty(device.getName()))
                    bluetoothHandler.devices.add(device);
                if (bluetoothHandler.getAdapter() != null)
                    bluetoothHandler.getAdapter().notifyDataSetChanged();
                if (!bluetoothHandler.scanning && bluetoothHandler.devices.size() > 0) {
                    bluetoothHandler.scanning = true;
                }
            }
        });
    }
}