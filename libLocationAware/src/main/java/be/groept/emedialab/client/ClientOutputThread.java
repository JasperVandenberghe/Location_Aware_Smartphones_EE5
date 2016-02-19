package be.groept.emedialab.client;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.communications.OutputThread;
import be.groept.emedialab.util.GlobalResources;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClientOutputThread extends OutputThread {

    private static final String TAG = "ClientOutputThread";

    public ClientOutputThread(OutputStream outputStream){
        super(outputStream);
    }

    @Override
    public void run() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            Log.i(TAG, "Sending position of this device.");

            // Write the id of the device
            String deviceAddress = GlobalResources.getInstance().getDevice().getId();
            if(deviceAddress == null){
                deviceAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
                GlobalResources.getInstance().getDevice().setId(deviceAddress);
            }

            //dataOutputStream.writeUTF(deviceAddress);

            synchronized (this) {
                //Wait for first "message" to arrive.
                wait();
            }
            while (keepRunning) {

                // Always send the coordinates
                DataHandler.sendData(dataOutputStream, DataHandler.DATA_TYPE_COORDINATES);

                // Send all available dataPackets for the server (UUID == "")
                DataPacket dataPacket;
                while((dataPacket = GlobalResources.getInstance().getDataForClient("")) != null){
                    DataHandler.sendData(dataOutputStream, dataPacket);
                }

                dataOutputStream.flush();

                synchronized (this) {
                    //Wait until new message has arrived.
                    wait();
                }

            }

            Log.i(TAG, "Closing Connection");
            dataOutputStream.close();
            Log.i(TAG, "OutputStream closed");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
