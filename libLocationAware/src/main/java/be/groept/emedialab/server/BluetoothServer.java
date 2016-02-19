package be.groept.emedialab.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import be.groept.emedialab.communications.InputThread;
import be.groept.emedialab.communications.OutputThread;
import be.groept.emedialab.util.GlobalResources;

public class BluetoothServer extends Thread {

    private static final String TAG = "BluetoothServer";
    private static final String NAME = "BluetoothApp";

    private BluetoothServerSocket mmServerSocket;
    volatile boolean isStarted = true;

    /**
     * Creates the BluetoothServer. This method assumes Bluetooth is available and enabled.
     */
    public BluetoothServer(Context context) {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mmServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));

            // Register for device disconnection
            IntentFilter bluetoothDisconnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        Log.d(TAG, "BluetoothServer is disconnecting " + device.getAddress());

                        GlobalResources.getInstance().removeConnectedDevice(device);
                    }
                }
            };
            context.registerReceiver(mReceiver, bluetoothDisconnectedFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            while(isStarted){
                Log.i(TAG, "Listening...");
                BluetoothSocket socket = mmServerSocket.accept();
                Log.i(TAG, "New Connection accepted");

                if (socket != null) {
                    manageConnectedSocket(socket);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            Log.d(TAG, "STOP LISTENING IS CALLED AND isSTARTED IS FALSE1111");
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket){
        try{
            String deviceAddress = socket.getRemoteDevice().getAddress();

            InputThread inputThread = new InputThread(socket.getInputStream(), deviceAddress);
            inputThread.start();

            OutputThread serverOutputThread = new ServerOutputThread(socket.getOutputStream(), deviceAddress);
            serverOutputThread.start();

            GlobalResources.getInstance().addConnectedDevice(socket.getRemoteDevice(), new SocketInputOutputTrio(socket, inputThread, serverOutputThread));

            Log.d(TAG, "Storing connection with device " + deviceAddress);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopListening(){
        Log.i(TAG, "STOP LISTENING");
        isStarted = false;
        try {
            mmServerSocket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.i(TAG, "STOP LISTENING HAPPENED! " + isStarted);
    }

    public void quit(){
        try{
            stopListening();
            Log.d(TAG, "[STOP] BluetoothServer is quitting.");
            GlobalResources.getInstance().removeConnectedDevices();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}