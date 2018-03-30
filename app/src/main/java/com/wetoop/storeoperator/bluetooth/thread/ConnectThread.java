package com.wetoop.storeoperator.bluetooth.thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.wetoop.storeoperator.bluetooth.BluetoothHandler;

import java.io.IOException;

import static com.wetoop.storeoperator.bluetooth.BluetoothHandler.MY_UUID;

public abstract class ConnectThread extends Thread {

    protected final BluetoothSocket socket;
    protected final BluetoothDevice device;
    protected final BluetoothHandler bluetoothHandler;

    public ConnectThread(BluetoothHandler bluetoothHandler, BluetoothDevice device) {
        this.device = device;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.socket = tmp;
        this.bluetoothHandler = bluetoothHandler;
        this.setName("ConnectThread");
    }

    public void run() {
        bluetoothHandler.bluetoothAdapter.cancelDiscovery();
        try {
            if (!socket.isConnected())
                socket.connect();
        } catch (IOException var5) {
            bluetoothHandler.connectionFailed();
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.start();
            return;
        }

        synchronized (this) {
            bluetoothHandler.connectThread = null;
        }

        bluetoothHandler.connected(this.socket);
    }

    public void cancel() {
        try {
            if (socket != null)
                this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}