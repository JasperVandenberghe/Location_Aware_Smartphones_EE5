package be.groept.emedialab.math;

import junit.framework.TestCase;

import org.junit.Test;
import org.opencv.core.Point;

import be.groept.emedialab.image_manipulation.PatternCoordinates;
import be.groept.emedialab.util.Point3D;

/**
 * Test class for methods in Calc class
 */
public class PositionCalculationTest extends TestCase {

    public void testCalculate(){
        //Always test this with phone 2 !!
        CameraConstants.getInstance().initPhone("867545010631055");
        PositionCalculation calc = new PositionCalculation(20, 640, 480, 50);
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0,0), new Point(10, 0), new Point(10, -10), 0.0
        );
        Point3D calculation = calc.patternToReal(patternCoordinates);
        assertEquals(-738, Math.round(calculation.getX()));
        assertEquals(-337, Math.round(calculation.getY()));
        double zCoordinate = 1280/(2*Math.tan(Math.toRadians(25)));
        assertEquals(zCoordinate, calculation.getZ());
    }

    public void testCalculateRotation(){
        PositionCalculation calc = new PositionCalculation(20, 640, 480, 50);
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0,0), new Point(10, 0), new Point(10, -10), 0.0
        );
        double rotation = calc.calculateRotation(patternCoordinates);
        assertEquals(90.0, rotation);

    }
}