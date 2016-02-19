package be.groept.emedialab.image_manipulation;

import android.util.Log;

import org.opencv.core.Point;

import java.io.Serializable;

/**
 * Holds the 4 coordinates of the corners of the pattern, and the rotation.
 */
public class PatternCoordinates implements Serializable{
    private static final long serialVersionUID = 2L;

    private Point num1;
    private Point num2;
    private Point num3;
    private Point num4;
    private double angle;
    private boolean patternFound = true;

    public PatternCoordinates(Point num1, Point num2, Point num3, Point num4, double angle){
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
        this.num4 = num4;
        this.angle = angle;
    }

    public PatternCoordinates(Point num1, Point num2, Point num3, Point num4, double angle, boolean patternFound){
        this(num1, num2, num3, num4, angle);
        this.patternFound = patternFound;
    }

    public Point getNum(int number){
        switch(number){
            case 2:
                return num2;
            case 3:
                return num3;
            case 4:
                return num4;
            default:
                return num1;
        }
    }

    public double getAngle() {
        return angle;
    }

    public boolean getPatternFound(){
        return patternFound;
    }

    public void setNum(int number, Point value){
        switch(number){
            case 2:
                this.num2 = value;
                break;
            case 3:
                this.num3 = value;
                break;
            case 4:
                this.num4 = value;
                break;
            default:
                this.num1 = value;
        }
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setPatternFound(boolean patternFound){
        this.patternFound = patternFound;
    }

    @Override
    public String toString(){
        String format = "(%.2f, %.2f) (%.2f, %.2f) (%.2f, %.2f) (%.2f, %.2f) (rot:%.2f) %b";
        return String.format(format,
                getNum(1).x, getNum(1).y,
                getNum(2).x, getNum(2).y,
                getNum(3).x, getNum(3).y,
                getNum(4).x, getNum(4).y,
                getAngle(),
                patternFound);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static PatternCoordinates flip(PatternCoordinates patternCoordinates){
        return new PatternCoordinates(
                //Flip the coordinates so they are in the right convention for the Calc class.
                new Point(patternCoordinates.getNum(1).y, patternCoordinates.getNum(1).x),
                new Point(patternCoordinates.getNum(2).y, patternCoordinates.getNum(2).x),
                new Point(patternCoordinates.getNum(3).y, patternCoordinates.getNum(3).x),
                new Point(patternCoordinates.getNum(4).y, patternCoordinates.getNum(4).x),
                patternCoordinates.getAngle()
        );
    }

    public static PatternCoordinates flipYaxis(PatternCoordinates patternCoordinates){
        return new PatternCoordinates(
                //Flip around the x-axis so the y-axis increments when we move upwards
                new Point(patternCoordinates.getNum(1).x, -patternCoordinates.getNum(1).y),
                new Point(patternCoordinates.getNum(2).x, -patternCoordinates.getNum(2).y),
                new Point(patternCoordinates.getNum(3).x, -patternCoordinates.getNum(3).y),
                new Point(patternCoordinates.getNum(4).x, -patternCoordinates.getNum(4).y),
                patternCoordinates.getAngle()
        );
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static PatternCoordinates flipBoth(PatternCoordinates patternCoordinates){
        return new PatternCoordinates(
                //Flip around the x-axis so the y-axis increments when we move upwards and flip x-y for Calc convention
                new Point(patternCoordinates.getNum(1).y, -patternCoordinates.getNum(1).x),
                new Point(patternCoordinates.getNum(2).y, -patternCoordinates.getNum(2).x),
                new Point(patternCoordinates.getNum(3).y, -patternCoordinates.getNum(3).x),
                new Point(patternCoordinates.getNum(4).y, -patternCoordinates.getNum(4).x),
                patternCoordinates.getAngle()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PatternCoordinates)) return false;

        PatternCoordinates pattern = (PatternCoordinates) obj;

        return this.getNum(1).equals(pattern.getNum(1)) &&
                this.getNum(2).equals(pattern.getNum(2)) &&
                this.getNum(3).equals(pattern.getNum(3)) &&
                this.getNum(4).equals(pattern.getNum(4)) &&
                this.getAngle() == pattern.getAngle() &&
                this.getPatternFound() == pattern.getPatternFound();
    }
}
