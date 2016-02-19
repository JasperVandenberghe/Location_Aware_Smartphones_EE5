package be.groept.emedialab.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import be.groept.emedialab.R;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    public BluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices){
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        BluetoothDevice device = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetoothdevice, parent, false);

        }
        TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView deviceAddress = (TextView) convertView.findViewById(R.id.deviceAddress);
        deviceName.setText(device.getName());
        String address = "\t\t\t" + device.getAddress();
        deviceAddress.setText(address);
        return convertView;
    }
}
