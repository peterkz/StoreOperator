package com.wetoop.storeoperator.bluetooth.v4;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;

import com.wetoop.storeoperator.bluetooth.v4.callback.WCScanCallback;
import com.wetoop.storeoperator.ui.dialog.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author WETOOP
 * @Date 2018/3/9.
 * @Description 5.0以上系统实现
 */

public class System_V5Support extends BH_V4Support {

    private WCScanCallback scanCallback = new WCScanCallback(this);
    private BluetoothLeScanner bluetoothLeScanner;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public System_V5Support(Context context, Handler handler) {
        super(context, handler);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void startScan() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.connect();
        }
        List<UUID> uuids = new ArrayList<>();
        for (String uuid : serviceUUIDs) {
            try {
                uuids.add(UUID.fromString(uuid));
                break;
            } catch (Exception e) {
            }
        }
        if (uuids.size() > 0 && bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            List<ScanFilter> filters = new ArrayList<>();
            ParcelUuid uuid = new ParcelUuid(uuids.get(0));
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(uuid).build();
            filters.add(filter);
            ScanSettings settings = new ScanSettings.Builder().build();
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stopScan() {
        LoadingDialog.hide();
        scanning = false;
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(scanCallback);
    }
}
