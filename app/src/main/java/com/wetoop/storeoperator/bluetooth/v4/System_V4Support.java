package com.wetoop.storeoperator.bluetooth.v4;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.wetoop.storeoperator.bluetooth.v4.callback.WCLeScanCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author WETOOP
 * @Date 2018/3/9.
 * @Description 5.0以下系统实现
 */

public class System_V4Support extends BH_V4Support {

    private WCLeScanCallback leScanCallback = new WCLeScanCallback(this);

    public System_V4Support(Context context, Handler handler) {
        super(context, handler);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
        bluetoothAdapter.startLeScan(uuids.toArray(new UUID[uuids.size()]), leScanCallback); //开始搜索
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopScan() {
        bluetoothAdapter.stopLeScan(leScanCallback);
        scanning = false;
    }
}
