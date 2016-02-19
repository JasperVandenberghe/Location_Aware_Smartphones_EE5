package be.groept.emedialab.communications;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;

/**
 * Class for handling the the types of data sent as well as converting objects to strings.
 * For communication between devices, use these ints and functions.
 */
public class DataHandler {

    private static final String TAG = "DataHandler";

    /**
     * Types of data. These are ints to keep them short (as opposed to long Strings with
     * the name of the package) because they are sent over the connection each time.
     */
    public static final int DATA_TYPE_COORDINATES = 0;
    public static final int DATA_TYPE_DATA_PACKET = 1;
    public static final int DATA_TYPE_PARTY_READY = 2;
    public static final int DATA_TYPE_START_GAME = 3;
    public static final int DATA_TYPE_PAUSE_GAME = 4;
    public static final int DATA_TYPE_END_GAME = 5;
    public static final int DATA_TYPE_DEVICE_CONNECTED = 6;
    public static final int DATA_TYPE_DEVICE_DISCONNECTED = 7;
    public static final int DATA_TYPE_OWN_POS_UPDATED = 8;

    /**
     * Reads in data from the provided DataInputStream and handles it
     * @param dataInputStream The stream that needs to be read
     * @param deviceAddress The device address, needed for handling the provided data
     */
    public static void readData(DataInputStream dataInputStream, String deviceAddress){
        try{
            int dataType = dataInputStream.readInt();
            Log.d(TAG, "Read in DataType: " + dataType);
            switch(dataType){

                case DataHandler.DATA_TYPE_COORDINATES:
                    double x = dataInputStream.readDouble();
                    double y = dataInputStream.readDouble();
                    double z = dataInputStream.readDouble();
                    double rotation = dataInputStream.readDouble();
                    boolean foundPattern = dataInputStream.readBoolean();
                    Position pos = new Position(x, y, z, rotation);
                    pos.setFoundPattern(foundPattern);
                    GlobalResources.getInstance().updateDevicePosition(deviceAddress, pos);
                    Log.d(TAG, "Read in data type coordinates: x[" + x + "] y[" + y + "] z[" + z + "] rot[" + rotation + "] + found[" + foundPattern + "] for device " + deviceAddress);
                    break;

                case DataHandler.DATA_TYPE_DATA_PACKET:
                    String serializedObject = dataInputStream.readUTF();
                    GlobalResources.getInstance().writeDataToInputBuffer(serializableFromString(serializedObject));
                    Log.d(TAG, "Read in dataPacket: " + serializableFromString(serializedObject).toString());
                    break;

                default:
                    GlobalResources.getInstance().alertify(dataType, null);
                    break;

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sends an empty DataPacket to the provided DataOutputStream
     * @param dataOutputStream Stream to write to
     * @param dataType Type of data that needs to be sent
     */
    public static void sendData(DataOutputStream dataOutputStream, int dataType){
        sendData(dataOutputStream, new DataPacket(dataType, null));
    }

    /**
     * Given a DataPacket sends certain values to the provided DataOutputStream.
     * @param dataOutputStream Stream to which it needs to be written
     * @param dataPacket Packet with the DataType that needs to be sent
     */
    public static void sendData(DataOutputStream dataOutputStream, DataPacket dataPacket){
        try{
            // Always write the DataType int
            dataOutputStream.writeInt(dataPacket.getDataType());

            Log.d(TAG, "Sending dataType " + dataPacket.getDataType());

            // In certain cases, also write some additional data
            switch (dataPacket.getDataType()) {

                case DataHandler.DATA_TYPE_DATA_PACKET:
                    Log.d(TAG, "Sending data packet!");
                    dataOutputStream.writeUTF(serializableToString(dataPacket.getOptionalData()));
                    dataOutputStream.flush();
                    break;

                case DataHandler.DATA_TYPE_COORDINATES:
                    Device device = GlobalResources.getInstance().getDevice();
                    Log.d(TAG, "Sending coordinates x[" + device.getPosition().getX() + "] y[" + device.getPosition().getY() + "] z[" + device.getPosition().getZ() + "] rot[" + device.getPosition().getRotation() + "] found[" + device.getPosition().getFoundPattern() + "]");
                    dataOutputStream.writeDouble(device.getPosition().getX());
                    dataOutputStream.writeDouble(device.getPosition().getY());
                    dataOutputStream.writeDouble(device.getPosition().getZ());
                    dataOutputStream.writeDouble(device.getPosition().getRotation());
                    dataOutputStream.writeBoolean(device.getPosition().getFoundPattern());
                    break;

                default:
                    break;

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Converts a String to a Serializable Object
     * @param string The String that needs to be converted to Serializable Object
     * @return Serializable Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Serializable serializableFromString( String string ) throws IOException, ClassNotFoundException {
        byte [] data = Base64.decode(string, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( data ) );
        Serializable serializable  = (Serializable) ois.readObject();
        ois.close();
        return serializable;
    }

    /**
     * Converts a Serializable Object to a String for sending through a DataStream
     * @param serializable The Serializable Object that needs to be converted to a String
     * @return The converted String
     * @throws IOException
     */
    public static String serializableToString( Serializable serializable ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( serializable );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

}
