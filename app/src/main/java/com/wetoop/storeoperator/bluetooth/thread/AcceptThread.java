package com.wetoop.storeoperator.bluetooth.thread;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.wetoop.storeoperator.bluetooth.BluetoothHandler;

import java.io.IOException;

import static com.wetoop.storeoperator.bluetooth.BluetoothHandler.MY_UUID;

public abstract class AcceptThread extends Thread {

    protected final BluetoothServerSocket socket;
    protected final BluetoothHandler bluetoothHandler;

    public AcceptThread(BluetoothHandler bluetoothHandler) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothHandler.bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTPrinter", MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = tmp;
        this.bluetoothHandler = bluetoothHandler;
    }

    @Override
    public void run() {
        BluetoothSocket socket = null;

        while (bluetoothHandler.mState != 3) {
            try {
                if (this.socket != null)
                    socket = this.socket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (socket != null) {
                synchronized (this) {
                    switch (bluetoothHandler.mState) {
                        case 0:
                        case 3:
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1:
                        case 2:
                            bluetoothHandler.connected(socket);
                    }
                }
            }
        }
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