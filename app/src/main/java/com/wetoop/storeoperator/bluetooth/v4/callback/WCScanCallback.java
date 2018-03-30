package com.wetoop.storeoperator.bluetooth.v4.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.text.TextUtils;

import com.wetoop.storeoperator.bluetooth.v4.BH_V4Support;

import java.util.List;

/**
 * @Author WETOOP
 * @Date 2018/3/9.
 * @Description
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WCScanCallback extends ScanCallback {

    private BH_V4Support supportHandler;

    public WCScanCallback(BH_V4Support supportHandler) {
        this.supportHandler = supportHandler;
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
    }

    @Override
    public void onScanFailed(int errorCode) {
        supportHandler.stopScan();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        final BluetoothDevice device = result.getDevice();
        supportHandler.handler.post(new Runnable() {
            @Override
            public void run() {
                if (!supportHandler.devices.contains(device) && !TextUtils.isEmpty(device.getName()))
                    supportHandler.devices.add(device);
                if (supportHandler.getAdapter() != null)
                    supportHandler.getAdapter().notifyDataSetChanged();
                if (!supportHandler.scanning && supportHandler.devices.size() > 0) {
                    supportHandler.scanning = true;
                }
            }
        });
    }
}
