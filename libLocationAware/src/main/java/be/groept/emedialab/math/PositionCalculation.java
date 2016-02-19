package be.groept.emedialab.math;

import android.util.Log;

import be.groept.emedialab.image_manipulation.PatternCoordinates;
import be.groept.emedialab.util.Point3D;
import be.groept.emedialab.util.Vector;

import org.opencv.core.Point;

/**
 * Computes the x,y,z coordinates of the device using the outline of the detected pattern.
 */
public class PositionCalculation {
    private static final String TAG = "Calc";

    //Defines the corner points of the pattern in pixels
    private double xap, xbp, xcp, xdp;
    private double yap, ybp, ycp, ydp;

    //Defines the angle of view of the camera
    private double phiX;

    //The real life side of the pattern in cm.
    private double patternSide;

    //Canvas X Size
    private double canvasXSize;

    private double fieldOfViewX;

    private double scaleFactor;

    /**
     * @param patternSide The real life patternSide of the pattern in cm (The length of one of the sides of the square)
     * @param width Width in pixels of the full image in portrait.
     * @param height Height in pixels of the full image in portrait.
     * @param phiX Angle of view of the camera, careful this is the vertical angle in phone x-y conventions
     */
     public PositionCalculation(double patternSide, double width, double height, double phiX) {
         this.patternSide = patternSide;
         this.phiX = phiX;
         canvasXSize = width;
         double canvasYSize = height;
    }

    /**
     * Calculate the x,y and z coordinates of the device.
     * Position (0,0,0) is when the pattern is in the center.
     *
     * @param pattern Corner points of the pattern.
     *                Num1 is the point at the white inner square.
     *                Moving clockwise the points should correspond to Num2, Num3 and Num4.
     *                X-axis is 480side, Y-axis is the 640side. Make sure the pattern is in the right coordinates!
     * @return A new point representing the position of the device.
     */
    public Point3D patternToReal(PatternCoordinates pattern){
        initPixelValues(pattern);
        calculateFieldOfView();

        //Calculate z using the field of view of y (higher accuracy than x-axis)
        double zCoordinate = fieldOfViewX / (2 * Math.tan(Math.toRadians(phiX / 2)));

        Point centerPattern = calculateCenterPattern();

        //Set the origin of the coordinate system of the image in the center of the sensor
        centerPattern.x -= 320;
        centerPattern.y -= 240;

        //Flip over x-axis
        centerPattern.y *= -1;

        //Factor in screen offset
        centerPattern.x -= (5.35 / scaleFactor);
        centerPattern.y -= (2 / scaleFactor);

        //Take the translated error of the camera into account
        double ex = (CameraConstants.getInstance().getEx() / CameraConstants.getInstance().getHeight()) * zCoordinate;
        double ey = (CameraConstants.getInstance().getEy() / CameraConstants.getInstance().getHeight()) * zCoordinate;
        Point translated = new Point(
                centerPattern.x + ex,
                centerPattern.y + ey
        );

        double rotation = calculateRotation(pattern);
        Point rotated = new Point(
                translated.x * Math.cos(Math.toRadians(rotation)) + translated.y * Math.sin(Math.toRadians(rotation)),
                -translated.x * Math.sin(Math.toRadians(rotation)) + translated.y * Math.cos(Math.toRadians(rotation))
        );

        //Convert pixel values to real values
        double xCoordinate = rotated.x * scaleFactor;
        double yCoordinate = rotated.y * scaleFactor;

        Log.d("Coordinates", -xCoordinate + ":" + -yCoordinate + ":" + zCoordinate + ":" + rotation);

        return new Point3D(-xCoordinate, -yCoordinate, zCoordinate);
    }

    private void initPixelValues(PatternCoordinates patternCoordinates){
        xap = patternCoordinates.getNum(1).x;
        yap = patternCoordinates.getNum(1).y;
        xbp = patternCoordinates.getNum(2).x;
        ybp = patternCoordinates.getNum(2).y;
        xcp = patternCoordinates.getNum(3).x;
        ycp = patternCoordinates.getNum(3).y;
        xdp = patternCoordinates.getNum(4).x;
        ydp = patternCoordinates.getNum(4).y;
    }

    private Point calculateCenterPattern(){
        return new Point(
                (xap + xbp + xcp + xdp)/4,
                (yap + ybp + ycp + ydp)/4
        );
    }

    private void calculateFieldOfView(){
        //Take average of all pattern sides to stabilize the readings
        double patternSideAB = Math.sqrt(Math.pow(xap - xbp, 2) + Math.pow(yap - ybp, 2));
        double patternSideBC = Math.sqrt(Math.pow(xbp - xcp, 2) + Math.pow(ybp - ycp, 2));
        double patternSideCD = Math.sqrt(Math.pow(xcp - xdp, 2) + Math.pow(ycp - ydp, 2));
        double patternSideDA = Math.sqrt(Math.pow(xdp - xap, 2) + Math.pow(ydp - yap, 2));
        double patternSidePx = (patternSideAB + patternSideBC + patternSideCD + patternSideDA) / 4.0;

        scaleFactor = patternSide/patternSidePx;

        fieldOfViewX = (patternSide / patternSidePx) * canvasXSize;
    }

    //TODO: this is called twice every run (once by PositionCalculation.patternToReal, once by PatternDetector.calculateCoordinates)
    public double calculateRotation(PatternCoordinates pixelPatternCoordinates){
        Point corner1 = pixelPatternCoordinates.getNum(1);
        Point corner2 = pixelPatternCoordinates.getNum(2);
        Point corner3 = pixelPatternCoordinates.getNum(3);
        Point corner4 = pixelPatternCoordinates.getNum(4);

        //Calculate rotation with the x-axis of the image coordinate system with the horizontal sides of the pattern
        Vector xAxis = new Vector(640, 0);
        Vector side14 = new Vector(corner4.x - corner1.x, corner4.y - corner1.y);
        Vector side23 = new Vector(corner3.x - corner2.x, corner3.y - corner2.y);

        double cos14 = side14.dotProduct(xAxis)/(side14.getLength() * xAxis.getLength());
        double cos23 = side23.dotProduct(xAxis)/(side23.getLength() * xAxis.getLength());

        //Calculate rotation with the y-axis of the image coordinate system with the vertical sides of the pattern
        //Needs to be -480 because we flipped the y-axis
        Vector yAxis = new Vector(0, -480);
        Vector side12 = new Vector(corner2.x - corner1.x, corner2.y - corner1.y);
        Vector side43 = new Vector(corner3.x - corner4.x, corner3.y - corner4.y);

        double cos12 = side12.dotProduct(yAxis)/(side12.getLength() * yAxis.getLength());
        double cos43 = side43.dotProduct(yAxis)/(side43.getLength() * yAxis.getLength());

        double cos = (cos14 + cos23 + cos12 + cos43)/4;

        double rotationAngle;
        if(corner4.y > corner1.y)
            rotationAngle = 360 - Math.toDegrees(Math.acos(cos));
        else
            rotationAngle = Math.toDegrees(Math.acos(cos));

        return rotationAngle;
    }
}
