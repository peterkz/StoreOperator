package com.wetoop.storeoperator.bluetooth.v3;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;
import com.wetoop.storeoperator.bluetooth.thread.AcceptThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectThread;
import com.wetoop.storeoperator.bluetooth.thread.ConnectedThread;

import java.util.ArrayList;

public class BH_V3Support extends BluetoothHandler {

    public BH_V3Support(Context context, Handler handler) {
        super(context, handler);
        this.devices = new ArrayList<>(this.bluetoothAdapter.getBondedDevices());
    }

    @Override
    public AcceptThread getAcceptThread() {
        return new AcceptThread(this) {
        };
    }

    @Override
    public ConnectThread getConnectThread(BluetoothDevice device) {
        return new ConnectThread(this, device) {
        };
    }

    @Override
    public ConnectedThread getConnectedThread(BluetoothSocket socket) {
        return new ConnectedThread(this, socket) {
        };
    }

    @Override
    public void showDeviceList(final AdapterView.OnItemClickListener listener) {
        View view = View.inflate(context, R.layout.print_dialog, null);
        ListView lv = (ListView) view.findViewById(R.id.print_dialog);
        if (adapter == null)
            adapter = getBluetoothPrinterAdapter();
        lv.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.setTitle("选择要连接的蓝牙")
                .setView(view).setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.onItemClick(adapterView, view, i, l);
                dialog.dismiss();
            }
        });
    }

}