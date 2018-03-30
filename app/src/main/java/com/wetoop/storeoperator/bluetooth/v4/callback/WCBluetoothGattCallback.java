package com.wetoop.storeoperator.bluetooth.v4.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.widget.Toast;

import com.wetoop.storeoperator.bluetooth.v4.BH_V4Support;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WCBluetoothGattCallback extends BluetoothGattCallback {


    private BH_V4Support bluetoothHandler;

    public WCBluetoothGattCallback(BH_V4Support bluetoothHandler) {
        this.bluetoothHandler = bluetoothHandler;
    }

    /**
     * 当连接上设备或者失去连接时会回调该函数
     *
     * @param gatt
     * @param status
     * @param newState
     */
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
            gatt.discoverServices(); //连接成功后就去找出该设备中的服务 private BluetoothGatt bluetoothGatt;
        }
    }

    /**
     * 当设备是否找到服务时，会回调该函数
     *
     * @param gatt
     * @param status
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        bluetoothHandler.bluetoothGatt = gatt;
        if (status == BluetoothGatt.GATT_SUCCESS) {   //找到服务了
            for (BluetoothGattService service : gatt.getServices()) {
                if (bluetoothHandler.serviceUUIDs.contains(service.getUuid().toString().toUpperCase())
                        || bluetoothHandler.serviceUUIDs.contains(service.getUuid().toString().toLowerCase()))
                    bluetoothHandler.services.add(service);
            }
            bluetoothHandler.connected(null);
        }
    }

    /**
     * 当读取设备时会回调该函数
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    /**
     * 当向设备Descriptor中写数据时，会回调该函数
     *
     * @param gatt
     * @param descriptor
     * @param status
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    /**
     * 设备发出通知时会调用到该接口
     *
     * @param gatt
     * @param characteristic
     */
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }

    /**
     * 当向Characteristic写数据时会回调该函数
     *
     * @param gatt
     * @param characteristic
     * @param status
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {// 写入成功
            if (bluetoothHandler.written) {
                gatt.disconnect();
                gatt.close();
                bluetoothHandler.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(bluetoothHandler.context, "打印完成", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                bluetoothHandler.connectedThread.write(null);
            }
        } else if (status == BluetoothGatt.GATT_FAILURE) { // 写入失败
        } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) { // 没有权限
        }
    }

}