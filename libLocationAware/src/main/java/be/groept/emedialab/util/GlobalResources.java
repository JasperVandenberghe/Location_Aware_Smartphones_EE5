package be.groept.emedialab.util;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import org.opencv.core.Mat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import be.groept.emedialab.communications.ClientBluetoothConnection;
import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.image_manipulation.ImageSettings;
import be.groept.emedialab.image_manipulation.PatternDetector;
import be.groept.emedialab.server.BluetoothServer;
import be.groept.emedialab.server.SocketInputOutputTrio;
import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.server.data.Position;

/**
 * Contains objects that need to be available to all Activities. This class is a Singleton meaning
 * the constructor cannot be called and the method 'getInstance' needs to be used instead.
 * 'getInstance()' will make a new GlobalResources Object if one does not exist yet. Or: If a
 * GlobalResources Object already exists it will return this instance. This way each Activity will
 * be dealing with the same GlobalResources instance.
 */
public class GlobalResources {

    private static final String TAG = "GlobalResources";

    private static GlobalResources instance;

    private ClientBluetoothConnection connection;
    private BluetoothServer bluetoothServer;

    /**
     * The handler that sends important information to the current activity. This should always be
     * updated to the new activity, so that it can know what has to be done.
     */
    private Handler handler = null;

    /**
     * Information about THIS device.
     */
    private Device device = new Device();
    private boolean moving = false;
    private boolean tilted = false;
    private boolean isClient = true;

    /**
     * Only Positions of other Devices.
     */
    private Map<String, Position> deviceList = new HashMap<>();

    /**
     * HashMap with the data that needs to be sent. The key is the id of the device, primarily
     * needed for the bluetoothServer. This is the outputBuffer.
     * In case of a client, the string of the server should be empty ("").
     */
    private HashMap<String, ArrayList<DataPacket>> ouputBuffer = new HashMap<>();

    /**
     * Contains all the active connections
     */
    private HashMap<String, SocketInputOutputTrio> connectedSockets = new HashMap<>();

    private ArrayList<Serializable> inputBuffer = new ArrayList<>();
    private PatternDetector patternDetector = null;
    private Mat image = null;

    private ImageSettings imageSettings = new ImageSettings();

    /**
     * Private constructor to prevent it from being called by external functions.
     */
    private GlobalResources(){}

    /**
     * This method should be called to get this singleton.
     * It is synchronized because two invocations of it cannot be interleaved - if a thread
     * calls this functions, other threads block until the first thread is finished.
     * @return The singleton of GlobalResources
     */
    public static synchronized GlobalResources getInstance(){
        if(instance == null){
            instance = new GlobalResources();
        }
        return instance;
    }

    public ClientBluetoothConnection getConnection() {
        return connection;
    }

    public void setConnection(ClientBluetoothConnection connection) {
        this.connection = connection;
    }

    public BluetoothServer getBluetoothServer() {
        return bluetoothServer;
    }

    public void setBluetoothServer(BluetoothServer bluetoothServer) {
        this.bluetoothServer = bluetoothServer;
    }

    public Map<String, Position> getDevices() {
        return deviceList;
    }

    /**
     * Only called by the Server. This is because only the server knows coordinates of other devices
     */
    public void updateDevicePosition(String deviceAddress, Position position){
        if(deviceList.containsKey(deviceAddress)){
            deviceList.put(deviceAddress, position);
            alertify(DataHandler.DATA_TYPE_COORDINATES, position);
        }else{
            Log.e(TAG, "Attempted to update device position, but device " + deviceAddress + " doesn't exist!");
        }
    }

    /**
     * Update position of current device.
     * @param position The position of the current device.
     */
    public void updateOwnPosition(Position position){
        device.setPosition(position);
        alertify(DataHandler.DATA_TYPE_OWN_POS_UPDATED, position);
        if(isClient) // Client needs to send coordinates to server
            sendData(DataHandler.DATA_TYPE_COORDINATES, null);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public PatternDetector getPatternDetector() {
        return patternDetector;
    }

    public void setPatternDetector(PatternDetector patternDetector) {
        this.patternDetector = patternDetector;
    }

    public void setMoving(boolean moving){
        this.moving = moving;
    }

    public boolean getMoving(){
        return moving;
    }

    public void setTilted(boolean tilted){
        this.tilted = tilted;
    }

    public boolean getTilted(){
        return tilted;
    }

    public void setClient(boolean client){
        this.isClient = client;
    }

    public boolean getClient(){
        return isClient;
    }

    public void setHandler(Handler handler){
        Log.d(TAG, "Handler is updated");
        this.handler = handler;
    }

    public Handler getHandler(){
        return handler;
    }

    /**
     * Send data to one or more connected devices
     * @param uuid null when sending to all devices, otherwise the UUID of the device the data
     *             is sent to
     * @param data data that is sent
     * @return if the data is stored successfully. Is false if device UUID was not found
     */
    public boolean sendData(String uuid, int dataType, Serializable data){
        if(uuid == null){ // send to all the connections
            Log.d(TAG, "Sending data packet to all clients.");
            for(Map.Entry<String, ArrayList<DataPacket>> entry : ouputBuffer.entrySet()){
                Log.d(TAG, "Sending to " + entry.getKey());

                // Store data
                entry.getValue().add(new DataPacket(dataType, data));

                // Notify OutputThread that data is available (initialises the real sending)
                connectedSockets.get(entry.getKey()).outputThread.sendData();
            }
            return true;
        }else{
            Log.d(TAG, "Adding data for uuid[" + uuid + "] data[" + data + "]");
            ArrayList<DataPacket> arrayList = ouputBuffer.get(uuid);
            if(arrayList != null){
                // Store data
                arrayList.add(new DataPacket(dataType, data));
                ouputBuffer.put(uuid, arrayList);

                // Notify OutputThread that data is available
                connectedSockets.get(uuid).outputThread.sendData();
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * Send data to all connected devices
     * @param dataType the type of data
     * @param data the Serializable data that needs to be sent
     * @return if the data is stored successfully
     */
    public boolean sendData(int dataType, Serializable data){
        return sendData(null, dataType, data);
    }

    public boolean sendData(DataPacket dataPacket){
        return sendData(null, DataHandler.DATA_TYPE_DATA_PACKET, dataPacket);
    }

    public boolean sendData(String uuid, Serializable data){
        return sendData(uuid, DataHandler.DATA_TYPE_DATA_PACKET, data);
    }

    public void writeDataToInputBuffer(Serializable serializable){
        Log.d(TAG, "Writing to inputBuffer " + serializable.toString());
        inputBuffer.add(serializable);

        // Notify that data is received!
        alertify(DataHandler.DATA_TYPE_DATA_PACKET, null);
    }

    public Serializable readData(){
        if(inputBuffer.size() > 0)
            return inputBuffer.remove(0);
        return null;
    }

    /**
     * Get the next data fragment needed by a client
     * @param uuid device UUID that it needs
     * @return the data that needs to be sent
     */
    public DataPacket getDataForClient(String uuid){
        ArrayList<DataPacket> arrayList = ouputBuffer.get(uuid);
        if(arrayList != null){
            if(arrayList.size() > 0) {
                Log.d(TAG, "Getting data for client " + uuid + " with size " + arrayList.size());
                return arrayList.remove(0);
            }
        }
        return null;
    }

    /**
     * Adds a device to the data buffer HashMap
     * @param deviceAddress the UUID of the device to be added
     */
    public void addDevice(String deviceAddress){
        Log.d(TAG, "Adding device " + deviceAddress);
        deviceList.put(deviceAddress, new Position());
        ouputBuffer.put(deviceAddress, new ArrayList<DataPacket>());
    }

    /**
     * Removes a device from the data buffer HashMap
     * @param uuid the UUID of the device that needs to be removed
     */
    public void removeDevice(String uuid){
        Log.d(TAG, "Removing device " + uuid);
        deviceList.remove(uuid);
        ouputBuffer.remove(uuid);
    }

    public HashMap<String, ArrayList<DataPacket>> getConnectedDevices(){
        return ouputBuffer;
    }

    /**
     * Adds a connected BluetoothDevice.
     * @param bluetoothDevice the device that needs to be added. This is a full BluetoothDevice
     *                        because name and MAC-address are needed in certain activities that
     *                        use this data.
     */
    public void addConnectedDevice(BluetoothDevice bluetoothDevice){
        addDevice(bluetoothDevice.getAddress());
        alertify(DataHandler.DATA_TYPE_DEVICE_CONNECTED, bluetoothDevice);

        Log.d(TAG, "Amount of connected devices: " + deviceList.size());
    }

    public void addConnectedDevice(BluetoothDevice bluetoothDevice, SocketInputOutputTrio socketInputOutputTrio){
        addConnectedDevice(bluetoothDevice);
        connectedSockets.put(bluetoothDevice.getAddress(), socketInputOutputTrio);
    }

    public void addConnectedDevice(String deviceAddress, SocketInputOutputTrio socketInputOutputTrio){
        connectedSockets.put(deviceAddress, socketInputOutputTrio);
    }

    /**
     * Remove a connected BluetoothDevice.
     * @param bluetoothDevice The device that needs to be removed. This is given as a
     *                        BluetoothDevice and not as a String directly so the handler
     *                        can notify with the full device.
     */
    public void removeConnectedDevice(BluetoothDevice bluetoothDevice){
        alertify(DataHandler.DATA_TYPE_DEVICE_DISCONNECTED, bluetoothDevice);
        removeConnectedDevice(bluetoothDevice.getAddress());
    }

    /**
     * Removes the connected device and cleans up its input thread, output thread and socket.
     * @param deviceAddress The MAC address of the device that needs to be removed.
     */
    public void removeConnectedDevice(String deviceAddress){
        removeDevice(deviceAddress);

        SocketInputOutputTrio socketInputOutputTrio = connectedSockets.get(deviceAddress);
        if(socketInputOutputTrio != null)
            socketInputOutputTrio.close();
        connectedSockets.remove(deviceAddress);
    }

    /**
     * Removes all the connected devices. Can be called when the server should stop being a server.
     */
    public void removeConnectedDevices(){
        Iterator it = connectedSockets.keySet().iterator();
        while(it.hasNext()){
            removeConnectedDevice((String) it.next());
            it.remove();
        }
    }

    /**
     * Sends a message to the handler that is currently connected
     * @param dataType the dataType
     * @param obj an optional object
     */
    public void alertify(int dataType, Object obj){
        if(handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = dataType;
            msg.obj = obj;
            handler.sendMessage(msg);
        }else{
            Log.e(TAG, "Handler is null!");
        }
    }

    public void updateImage(Mat image){
        this.image = image;
    }

    public Mat getImage(){
        return image;
    }

    public ImageSettings getImageSettings(){
        return imageSettings;
    }

}
