package be.groept.emedialab.server.data;

import java.io.Serializable;

public class Position implements Serializable {
    private static final long serialVersionUID = 2L;
    private double x;
    private double y;
    private boolean foundPattern = false;

    /**
     * Rotation in degrees.
     */
    private double rotation;

    /**
     * Distance from camera to code.
     */
    private double z;

    public Position(){}

    public Position(double x, double y, double z, double rotation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    public Position(double x, double y, double z, double rotation, boolean found){
        this(x, y, z, rotation);
        this.foundPattern = found;
    }

    //<editor-fold desc="Getters_Setters">
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean getFoundPattern(){
        return foundPattern;
    }


    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void setFoundPattern(boolean foundPattern){
        this.foundPattern = foundPattern;
    }

    //</editor-fold>

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.rotation) ^ (Double.doubleToLongBits(this.rotation) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Position other = (Position) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.rotation) != Double.doubleToLongBits(other.rotation)) {
            return false;
        }
        return Double.doubleToLongBits(this.z) == Double.doubleToLongBits(other.z);
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + "), alpha(" + getRotation() + ") " + foundPattern;
    }
}
