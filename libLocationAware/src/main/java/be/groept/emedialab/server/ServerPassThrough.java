package be.groept.emedialab.server;

import android.os.Handler;

import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.util.GlobalResources;

/**
 * Passes the position of the server to the server.
 * This provides the same functionality as the client code, but without sending the data.
 * Data is injected directly using the static reference to the server instance.
 */
public class ServerPassThrough {
    private long sampleRate = 500;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable(){
        @Override
        public void run() {
            try {
                BluetoothServer server = GlobalResources.getInstance().getBluetoothServer();
                Device device = GlobalResources.getInstance().getDevice();
                //add() is called, but behind the scenes it's a list.put() that overrides the previous value with that key
                //server.getDevices().add("0001", device.getPosition(), "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            timerHandler.postDelayed(timerRunnable, sampleRate);
        }
    };

    public void startPolling(){
        timerHandler.postDelayed(timerRunnable, this.sampleRate);
    }

    public void stopPolling(){
        timerHandler.removeCallbacks(timerRunnable);
    }
}
