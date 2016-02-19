package be.groept.emedialab.communications;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import be.groept.emedialab.client.BluetoothClient;
import be.groept.emedialab.util.ConnectionException;

public class ClientBluetoothConnection {

    private static final String TAG = "BTConnection";

    //Target device to connect with
    private BluetoothDevice bluetoothDevice;
    private BluetoothClient client;
    private Context context;

    public ClientBluetoothConnection(Context context, BluetoothDevice targetDevice){
        this.context = context;
        bluetoothDevice = targetDevice;
    }

    public void disconnect(){
        client.quit();
    }

    public BluetoothClient getClient(){
        return client;
    }

    public void setClient(BluetoothClient client){
        this.client = client;
    }

    public Context getContext(){
        return context;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void connect() throws ConnectionException {
        try {
            final Object mutex = new Object();

            Log.i(TAG, bluetoothDevice.getAddress());

            BluetoothClient client = new BluetoothClient(mutex, context, bluetoothDevice);
            waitForConnection(client, mutex);
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new ConnectionException("No Bluetooth devices connected", e);
        }
    }

    public void waitForConnection(BluetoothClient client, Object mutex) throws ConnectionException{
        client.execute();
        Log.i(TAG, "[ BLOCKED ] Waiting for connection to be established.");
        synchronized (mutex){
            //Avoid sending packets while the connection is still being established.
            try{
                mutex.wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        if(!client.isConnected())
            throw new ConnectionException("Connection failed. Client is not connected");

        Log.i(TAG, "[ OK ]");

        this.client = client;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        bluetoothDevice = device;
    }
}
