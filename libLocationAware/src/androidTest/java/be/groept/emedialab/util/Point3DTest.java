package be.groept.emedialab.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by Yoika on 6/10/2015.
 */
public class Point3DTest extends TestCase {

    @Test
    public void testGetX(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        assertEquals(5.0, point.getX());
    }

    @Test
    public void testGetY(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        assertEquals(4.0, point.getY());
    }

    @Test
    public void testGetZ(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        assertEquals(3.0, point.getZ());
    }

    @Test
    public void testSetX(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        point.setX(6.0);
        assertEquals(6.0, point.getX());
    }

    @Test
    public void testSetY(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        point.setY(7.0);
        assertEquals(7.0, point.getY());
    }

    @Test
    public void testSetZ(){
        Point3D point = new Point3D(5.0, 4.0, 3.0);
        point.setZ(8.0);
        assertEquals(8.0, point.getZ());
    }
}