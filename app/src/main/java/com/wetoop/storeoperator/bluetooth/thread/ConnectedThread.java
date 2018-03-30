package com.wetoop.storeoperator.bluetooth.thread;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ConnectedThread extends Thread {

    protected BluetoothSocket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected BluetoothHandler bluetoothHandler;

    public ConnectedThread(BluetoothHandler bluetoothHandler, BluetoothSocket socket) {
        if (socket != null) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.inputStream = tmpIn;
            this.outputStream = tmpOut;
        }
        this.bluetoothHandler = bluetoothHandler;
        this.setName("ConnectedThread");
    }

    public void run() {
        try {
            while (true) {
                byte[] e = new byte[256];
                int bytes = this.inputStream.read(e);
                if (bytes <= 0) {
                    bluetoothHandler.connectionLost();
                    if (bluetoothHandler.mState != 0) {
                        this.start();
                    }
                    break;
                }
                bluetoothHandler.handler.obtainMessage(2, bytes, -1, e).sendToTarget();
            }
        } catch (IOException var3) {
            bluetoothHandler.connectionLost();
            if (bluetoothHandler.mState != 0) {
                this.start();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            outputStream.write(buffer);
            outputStream.flush();
            bluetoothHandler.handler.obtainMessage(3, -1, -1, buffer).sendToTarget();
        } catch (IOException var3) {
            Toast.makeText(App.getInstance(), var3.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void cancel() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}