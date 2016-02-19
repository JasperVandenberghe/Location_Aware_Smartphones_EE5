package be.groept.emedialab.server.data;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Created by Yoika on 6/10/2015.
 */
public class DeviceTest extends TestCase {

    @Test
    public void testGetId(){
        Device device = new Device();
        assertEquals(null, device.getId());
        Device device1 = new Device("002");
        assertEquals("002", device1.getId());
    }

    @Test
    public void testSetId(){
        Device device = new Device();
        device.setId("003");
        assertEquals("003", device.getId());
    }

    @Test
    public void testSetPosition(){
        Position position = new Position(5.0, 4.0, 3.0, 0.0);
        Device device = new Device();
        device.setPosition(position);

        assertEquals(5.0, device.getPosition().getX());
        assertEquals(4.0, device.getPosition().getY());
        assertEquals(3.0, device.getPosition().getZ());
        assertEquals(0.0, device.getPosition().getRotation());
    }

}