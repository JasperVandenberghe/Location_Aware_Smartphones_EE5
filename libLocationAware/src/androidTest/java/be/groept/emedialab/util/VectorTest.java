package be.groept.emedialab.util;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by Yoika on 6/10/2015.
 */
public class VectorTest extends TestCase {

    @Test
    public void testGetX(){
        Vector vector = new Vector(5.0, 6.0);
        assertEquals(5.0, vector.getX());
    }

    @Test
    public void testGetY(){
        Vector vector = new Vector(5.0, 6.0);
        assertEquals(6.0, vector.getY());
    }

    @Test
    public void testNormalize(){
        Vector vector = new Vector(5.0, 6.0);
        vector.normalize();
        assertEquals(5.0/Math.sqrt(61), vector.getX());
        assertEquals(6.0/Math.sqrt(61), vector.getY());
    }

    @Test
    public void testGetLength(){
        Vector vector = new Vector(5.0, 6.0);
        assertEquals(Math.sqrt(61), vector.getLength());
    }

    @Test
    public void testDotProduct(){
        Vector vector = new Vector(5.0, 6.0);
        Vector vector2 = new Vector(5.0, 6.0);
        assertEquals(61.0, vector.dotProduct(vector2));
    }

}