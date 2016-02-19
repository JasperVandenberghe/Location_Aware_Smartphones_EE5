package be.groept.emedialab.server;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import be.groept.emedialab.communications.InputThread;
import be.groept.emedialab.communications.OutputThread;

/**
 * Class that holds the input and output thread of the server.
 * Both threads must already be running.
 */
public class SocketInputOutputTrio {

    public BluetoothSocket bluetoothSocket;
    public InputThread inputThread;
    public OutputThread outputThread;

    public static final int SERVER_INPUT_THREAD_CLOSED = 0;
    public static final int SERVER_OUTPUT_THREAD_CLOSED = 1;

    private boolean inputThreadOpen = true;
    private boolean outputThreadOpen = true;

    public SocketInputOutputTrio(BluetoothSocket bluetoothSocket, InputThread inputThread, OutputThread outputThread){
        this.bluetoothSocket = bluetoothSocket;
        this.inputThread = inputThread;
        this.outputThread = outputThread;
    }

    public void close(){
        Log.d("SocketInputOutputTrio", "[STOP] closing connection of " + bluetoothSocket.getRemoteDevice().getAddress() + " [" + bluetoothSocket.getRemoteDevice().getName() + "]");
        inputThread.stopRunning(handler);
        outputThread.stopRunning(handler);
    }

    private void closeSocket(){
        if(!inputThreadOpen && !outputThreadOpen){
            try {
                bluetoothSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == SERVER_INPUT_THREAD_CLOSED){
                inputThreadOpen = false;
                closeSocket();
            }else if(msg.what == SERVER_OUTPUT_THREAD_CLOSED){
                outputThreadOpen = false;
                closeSocket();
            }
        }
    };
}
