package be.groept.emedialab.communications;

import android.os.Handler;

import java.io.OutputStream;

public class OutputThread extends Thread {

    protected volatile boolean keepRunning = true;
    protected OutputStream outputStream;
    private Handler handler = null;

    public OutputThread(OutputStream outputStream){
        this.outputStream = outputStream;
    }

    public void sendData(){
        synchronized (this){
            notify();
        }
    }

    public void stopRunning(){
        keepRunning = false;
    }

    public void stopRunning(Handler handler){
        this.handler = handler;
        stopRunning();
    }

}
