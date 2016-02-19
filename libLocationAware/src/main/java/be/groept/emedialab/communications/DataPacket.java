package be.groept.emedialab.communications;

import android.provider.ContactsContract;

import java.io.Serializable;

/**
 * Class that holds an int for a dataType as well as an optional optionalData Serializable
 * object. This is used to store actions that need to be sent to other devices.
 */
public class DataPacket implements Serializable {

    private int dataType;
    private Serializable optionalData;

    public DataPacket(int dataType){
        this(dataType, null);
    }

    public DataPacket(Serializable data){
        this(DataHandler.DATA_TYPE_DATA_PACKET, data);
    }

    public DataPacket(int dataType, Serializable optionalData){
        this.dataType = dataType;
        this.optionalData = optionalData;
    }

    /**
     * Getter for dataType
     * @return the dataType
     */
    public int getDataType(){
        return dataType;
    }

    /**
     * Getter for optionalData
     * @return the optionalData
     */
    public Serializable getOptionalData(){
        return optionalData;
    }

}
