package be.groept.emedialab.client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import be.groept.emedialab.communications.InputThread;
import be.groept.emedialab.communications.OutputThread;
import be.groept.emedialab.fragments.ClientFragment;
import be.groept.emedialab.server.SocketInputOutputTrio;
import be.groept.emedialab.util.ConnectionException;
import be.groept.emedialab.util.GlobalResources;

import java.io.IOException;
import java.util.UUID;

/**
 * BluetoothClient is an implementation for connections using the Bluetooth connection. Android
 * Bluetooth-sockets are used, which are nearly identical in use to the Java Network Sockets. The
 * BluetoothSocket and Socket classes do not have a common super class. But they do use the same
 * Input and OutputStreams.
 */
public class BluetoothClient extends AsyncTask<Void, String, Void> {

    private static final String TAG = "BluetoothClient";
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private Context context;
    private final Object mutex;

    private boolean isConnected;

    public BluetoothClient(Object mutex, Context context, BluetoothDevice device) {
        this.mutex = mutex;
        this.context = context;
        this.device = device;
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    /*public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void update(String string){
        this.publishProgress(string);
    }*/

    @Override
    public Void doInBackground(Void... voids){
        try{
            isConnected = start();
            Log.i(TAG, "[ BUSY ] Preparing to release lock.");
            //The notify must be done _AFTER_ 'isConnected' is assigned.
            synchronized (mutex){
                mutex.notify();
                Log.i(TAG, "[ OK ] Released lock");
            }
        } catch (ConnectionException e){
            e.printStackTrace();
            //Toast.makeText(context, "Client End: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }
    /**
     * @return True when connection successful, false when failed.
     * @throws ConnectionException
     */
    public boolean start() throws ConnectionException {
        try {
            Log.i(TAG, "[ - ] Bluetooth connecting...");

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            // MY_UUID is the app's UUID string, also used by the server code
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
            socket.connect();

            if (socket.isConnected()) {
                Log.i(TAG, "Connection Successful.");

                Log.i(TAG, "Creating ClientOutputThread");
                OutputThread clientOutputThread = new ClientOutputThread(socket.getOutputStream());

                Log.i(TAG, "Creating ClientInputThread");
                InputThread inputThread = new InputThread(socket.getInputStream(), "");

                Log.i(TAG, "Starting input and output threads.");
                clientOutputThread.start();
                inputThread.start();

                GlobalResources.getInstance().addConnectedDevice("", new SocketInputOutputTrio(socket, null, clientOutputThread));

                isConnected = true;
            } else {
                Log.e(TAG, "Connection failed.");
                isConnected = false;
            }
            return isConnected;
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    public void quit(){
        GlobalResources.getInstance().removeConnectedDevice("");
        try {
            if(socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        ClientFragment.connectionDone(isConnected, context);
    }

    public boolean isConnected(){
        return isConnected;
    }
}
