package be.groept.emedialab.communications;


import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import be.groept.emedialab.server.SocketInputOutputTrio;

public class InputThread extends Thread {

    private static final String TAG = "InputThread";

    protected InputStream inputStream;
    protected String deviceAddress;
    protected volatile boolean running = true;
    protected Handler mHandler = null;

    public InputThread(InputStream inputStream, String deviceAddress){
        this.inputStream = inputStream;
        this.deviceAddress = deviceAddress;
    }

    @Override
    public void run() {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        Log.i(TAG, "Listening for data");

        try {
            while (running) {
                while(dataInputStream.available() > 0) {
                    Log.d(TAG, "Data available");
                    DataHandler.readData(dataInputStream, deviceAddress);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            dataInputStream.close();
            Log.i(TAG, "[Closed]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mHandler != null)
            mHandler.sendEmptyMessage(SocketInputOutputTrio.SERVER_INPUT_THREAD_CLOSED); //Only sends What

    }

    public void stopRunning(Handler mHandler){
        this.mHandler = mHandler;
        stopRunning();
    }

    public void stopRunning(){
        running = false;
    }

}
