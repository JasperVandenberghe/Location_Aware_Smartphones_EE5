package be.groept.emedialab.client;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import be.groept.emedialab.communications.DataHandler;

public class ClientInputThread extends Thread{

    private static final String TAG = "ClientInputThread";

    private boolean keepRunning = true;
    private InputStream inputStream;

    public InputStream getInputStream() {
        return inputStream;
    }

    public ClientInputThread(InputStream inputStream){
        super("Input_Thread");
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        DataInputStream dataInputStream = new DataInputStream(this.getInputStream());
        Log.i(TAG, "Listening for data");

        try {
            while (keepRunning) {
                while(dataInputStream.available() > 0) {
                    Log.d(TAG, "Data available");
                    DataHandler.readData(dataInputStream, "");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            dataInputStream.close();
            Log.i(TAG, "InputStream Closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRunning(){
        keepRunning = false;
    }
}
