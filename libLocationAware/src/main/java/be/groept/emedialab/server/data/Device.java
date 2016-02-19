package be.groept.emedialab.server.data;

import java.io.Serializable;

public class Device implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * Unique ID to identify this phone among the other connected devices.
     */
    private String id = null;
    private String mac = null;

    /**
     * Current position of the device.
     */
    private Position position = new Position();

    public Device(){
    }

    public Device(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMac(){
        return mac;
    }

    public void setMac(String mac){
        this.mac = mac;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Device other = (Device) obj;
        return this.id.equals(other.id);
    }
}