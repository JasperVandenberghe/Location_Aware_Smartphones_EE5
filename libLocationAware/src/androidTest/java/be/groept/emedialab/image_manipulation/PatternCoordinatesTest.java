package be.groept.emedialab.image_manipulation;

import junit.framework.TestCase;

import org.junit.Test;
import org.opencv.core.Point;

/**
 * Created by Yoika on 6/10/2015.
 */
public class PatternCoordinatesTest extends TestCase {

    @Test
    public void testGetNum(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        Point point1 = patternCoordinates.getNum(1);
        Point point2 = patternCoordinates.getNum(2);
        Point point3 = patternCoordinates.getNum(3);
        Point point4 = patternCoordinates.getNum(4);

        assertEquals(0.0, point1.x);
        assertEquals(-10.0, point1.y);
        assertEquals(0.0, point2.x);
        assertEquals(0.0, point2.y);
        assertEquals(10.0, point3.x);
        assertEquals(0.0, point3.y);
        assertEquals(10.0, point4.x);
        assertEquals(-10.0, point4.y);
    }

    @Test
    public void testGetAngle(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        assertEquals(0.0, patternCoordinates.getAngle());
    }

    @Test
    public void testSetNum(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        patternCoordinates.setNum(1, new Point(10, 10));

        assertEquals(10.0, patternCoordinates.getNum(1).x);
        assertEquals(10.0, patternCoordinates.getNum(1).y);
    }

    @Test
    public void testSetAngle(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        patternCoordinates.setAngle(90);

        assertEquals(90.0, patternCoordinates.getAngle());
    }

    @Test
    public void testGetPatternFound(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        assertEquals(true, patternCoordinates.getPatternFound());
    }

    @Test
    public void testSetPatternFound(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        patternCoordinates.setPatternFound(false);
        assertEquals(false, patternCoordinates.getPatternFound());
    }

    @Test
    public void testFlip(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        PatternCoordinates pattern = patternCoordinates.flip(patternCoordinates);

        assertEquals(-10.0, pattern.getNum(1).x);
        assertEquals(0.0, pattern.getNum(1).y);
        assertEquals(0.0, pattern.getNum(2).x);
        assertEquals(0.0, pattern.getNum(2).y);
        assertEquals(0.0, pattern.getNum(3).x);
        assertEquals(10.0, pattern.getNum(3).y);
        assertEquals(-10.0, pattern.getNum(4).x);
        assertEquals(10.0, pattern.getNum(4).y);
    }

    @Test
    public void testFlipYAxis(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        PatternCoordinates pattern = patternCoordinates.flipYaxis(patternCoordinates);

        assertEquals(0.0, pattern.getNum(1).x);
        assertEquals(10.0, pattern.getNum(1).y);
        assertEquals(0.0, pattern.getNum(2).x);
        assertEquals(-0.0, pattern.getNum(2).y);
        assertEquals(10.0, pattern.getNum(3).x);
        assertEquals(-0.0, pattern.getNum(3).y);
        assertEquals(10.0, pattern.getNum(4).x);
        assertEquals(10.0, pattern.getNum(4).y);
    }

    @Test
    public void testFlipBoth(){
        PatternCoordinates patternCoordinates = new PatternCoordinates(
                new Point(0, -10), new Point(0, 0), new Point(10, 0), new Point(10, -10), 0.0
        );
        PatternCoordinates pattern = patternCoordinates.flipBoth(patternCoordinates);

        assertEquals(-10.0, pattern.getNum(1).x);
        assertEquals(-0.0, pattern.getNum(1).y);
        assertEquals(0.0, pattern.getNum(2).x);
        assertEquals(-0.0, pattern.getNum(2).y);
        assertEquals(0.0, pattern.getNum(3).x);
        assertEquals(-10.0, pattern.getNum(3).y);
        assertEquals(-10.0, pattern.getNum(4).x);
        assertEquals(-10.0, pattern.getNum(4).y);
    }
}