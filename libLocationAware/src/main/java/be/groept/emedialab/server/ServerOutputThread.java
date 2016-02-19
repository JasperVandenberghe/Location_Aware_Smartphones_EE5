package be.groept.emedialab.server;

import android.os.Handler;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.communications.OutputThread;
import be.groept.emedialab.util.GlobalResources;

/**
 * ServerOutputThread
 */
public class ServerOutputThread extends OutputThread {

    private static final String TAG = "ServerOutputThread";
    private String deviceAddress;
    private Handler handler = null;

    public ServerOutputThread(OutputStream outputStream, String deviceAddress){
        super(outputStream);
        this.deviceAddress = deviceAddress;
    }

    @Override
    public void run(){
        try{
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            synchronized (this) {
                wait();
            }

            while(keepRunning){
                DataPacket data;
                while((data = GlobalResources.getInstance().getDataForClient(deviceAddress)) != null){
                    Log.d("ServerOutputThread", "Sending data to client!");
                    DataHandler.sendData(dataOutputStream, data);
                }
                dataOutputStream.flush();

                synchronized (this) {
                    wait();
                }
            }

            dataOutputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            handler.sendEmptyMessage(SocketInputOutputTrio.SERVER_OUTPUT_THREAD_CLOSED);
        }
    }

}
