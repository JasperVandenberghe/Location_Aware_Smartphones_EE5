package be.groept.emedialab.math;

import junit.framework.TestCase;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.server.data.Position;

/**
 * Test class for DistanceCalculation class
 */
public class DistanceCalculationTest extends TestCase {

    public void testGetDistancePoint(){
        Point point1 = new Point(0, 0);
        Point point2 = new Point(5, 0);
        assertEquals(5.0, DistanceCalculation.getDistance(point1, point2));
    }

    public void testGetDistanceDevice(){
        Device device1 = new Device();
        device1.setPosition(new Position(0, 0, 0, 0));
        Device device2 = new Device();
        device2.setPosition(new Position(5, 0, 0, 0));
        assertEquals(5.0, DistanceCalculation.getDistance(device1, device2));
    }

    public void testIsNextToPoint(){
        Point point1 = new Point(0, 0);
        Point point2 = new Point(5, 0);
        assertEquals(true, DistanceCalculation.isNextTo(point1, point2));
    }

    public void testIsNextToDevice(){
        Device device1 = new Device();
        device1.setPosition(new Position(0, 0, 0, 0));
        Device device2 = new Device();
        device2.setPosition(new Position(5, 0, 0, 0));
        assertEquals(true, DistanceCalculation.isNextTo(device1, device2));
    }

    public void testGetGroups(){
        ArrayList<Device> devices = new ArrayList<>();
        Device device1 = new Device();
        device1.setId("1");
        device1.setPosition(new Position(2, 0, 0, 0));
        devices.add(device1);
        Device device2 = new Device();
        device2.setId("2");
        device2.setPosition(new Position(15, 0, 0, 0));
        devices.add(device2);
        Device device3 = new Device();
        device3.setId("3");
        device3.setPosition(new Position(7, 0, 0, 0));
        devices.add(device3);
        Device device4 = new Device();
        device4.setId("4");
        device4.setPosition(new Position(40, 0, 0, 0));
        devices.add(device4);
        Device device5 = new Device();
        device5.setId("5");
        device5.setPosition(new Position(60, 0, 0, 0));
        devices.add(device5);

        ArrayList<ArrayList<Device>> groups = DistanceCalculation.getGroups(devices);
        assertEquals(3, groups.size());
        for(ArrayList<Device> group: groups){
            if(group.size() == 3){
                assertEquals(true, group.contains(device1));
                assertEquals(true, group.contains(device2));
                assertEquals(true, group.contains(device3));
            }
        }
    }

    /**
     * Basic test method, could be expanded but to be sure that newer code still supports the test
     * method, a better generalized form of expected value needs to be determined. For this reason,
     * only the amount of returned devices is checked (but the order can also be checked safely).
     * Checking line parameters is more tricky due to this still being unstandardized.
     */
    public void testGetLine(){
        Map<String, Position> devices = new HashMap<>();

        devices.put("device1", new Position(0, 0, 0, 90, true));
        devices.put("device2", new Position(5, 5, 0, 90, true));
        devices.put("device3", new Position(10, 10, 0, 90, true));
        devices.put("device4", new Position(15, 15, 0, 90, true));

        LinkedHashMap<String, Point> sorted = DistanceCalculation.getLine(devices);

        assertEquals(sorted.size(), 4);
    }
}
