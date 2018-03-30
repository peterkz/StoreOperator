package com.wetoop.storeoperator.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class BluetoothPrinterAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> devices;

    public BluetoothPrinterAdapter(Context context, List<BluetoothDevice> devices) {
        mContext = context;
        if (devices != null) {
            this.devices = devices;
        }
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        if (devices.size() > 0)
            return devices.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        //unique identifier
        return getItem(position).getAddress().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //PS. no we don't use the viewholder pattern since this a small data set < 100
        //and is no ui intensive either
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        }

        BluetoothDevice device = getItem(position);
        String deviceAlias = device.getName();
        try {
            Method method = device.getClass().getMethod("getAliasName");
            if (method != null) {
                deviceAlias = (String) method.invoke(device);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        TextView textView = (TextView) convertView;
        textView.setText(deviceAlias);

        return convertView;
    }

}
