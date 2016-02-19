package be.groept.emedialab.util;

public class Vector {
    private double x;
    private double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void normalize(){
        double length = getLength();
        x /= length;
        y /= length;
    }

    public double getLength(){
        return Math.sqrt(x * x + y * y);
    }

    public double dotProduct(Vector otherVector){
        return x * otherVector.x + y * otherVector.y;
    }

    /**
     * @return 0 when the two vectors are parallel (0 or 180degrees)
     */
    public double crossProduct(Vector otherVector){
        return (getX() * otherVector.getY() - getY() * otherVector.getX());
    }
}
