package be.groept.emedialab.server.data;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by Yoika on 6/10/2015.
 */
public class PositionTest extends TestCase {

    @Test
    public void testGetX(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        assertEquals(5.0, position.getX());
    }

    @Test
    public void testGetY(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        assertEquals(4.0, position.getY());
    }

    @Test
    public void testGetZ(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        assertEquals(3.0, position.getZ());
    }

    @Test
    public void testGetRotation(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        assertEquals(0.0, position.getRotation());
    }

    @Test
    public void testSetX(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        position.setX(6.0);
        assertEquals(6.0, position.getX());
    }

    @Test
    public void testSetY(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        position.setY(7.0);
        assertEquals(7.0, position.getY());
    }

    @Test
    public void testSetZ(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        position.setZ(8.0);
        assertEquals(8.0, position.getZ());
    }

    @Test
    public void testSetRotation(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        position.setRotation(90.0);
        assertEquals(90.0, position.getRotation());
    }

}