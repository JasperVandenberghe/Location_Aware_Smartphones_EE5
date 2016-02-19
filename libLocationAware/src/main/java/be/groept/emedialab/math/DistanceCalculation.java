package be.groept.emedialab.math;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import be.groept.emedialab.server.data.Device;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;

/**
 * Class to calculate the distance between different devices
 */
public class DistanceCalculation {

    private final static String TAG = "DistanceCalculation";

    public static final double MARGIN_NARROW = 1.0;
    public static final double MARGIN_MIDDLE = 3.0;
    public static final double MARGIN_WIDE = 8.0;

    private static final double DISTANCE_TOLERATION = 5.0;
    private static final double DISTANCE_BETWEEN_TOLERATION = 12.0;
    private static final double ANGLE_TOLERATION = 45;

    public static double getDistance(Point point1, Point point2){
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }

    public static double getDistance(Device device1, Device device2){
        Position position1 = device1.getPosition();
        Position position2 = device2.getPosition();

        return Math.sqrt(Math.pow(position1.getX() - position2.getX(), 2) + Math.pow(position1.getY() - position2.getY(), 2));
    }

    public static double getDistance(Position position1, Position position2){
        return Math.sqrt(Math.pow(position1.getX() - position2.getX(), 2) + Math.pow(position1.getY() - position2.getY(), 2));
    }

    public static boolean isNextTo(Point point1, Point point2){
        return getDistance(point1, point2) < 8;
    }

    public static boolean isNextTo(Device device1, Device device2){
        return getDistance(device1, device2) < 10;
    }

    public static boolean isNextTo(Position position1, Position position2){
        return getDistance(position1, position2) < 10;
    }

    public static boolean isNextToAndOriented(Position position1, Position position2){
        double rotation1 = position1.getRotation();
        double rotation2 = position2.getRotation();
        double rotationDiff = Math.abs(rotation1 - rotation2);
        return ( isNextTo(position1, position2) && rotationDiff < 20 && Math.abs(position1.getZ() - position2.getZ()) <= 5);
    }

    public static boolean isNextToHorizontal(Position position1, Position position2, double margin){
        double rotation1 = position1.getRotation();
        double rotation2 = position2.getRotation();
        //Rotate the position back to the coordinate system of the phone
        Point coordinates1 = new Point(
                position1.getX() * Math.cos(Math.toRadians(rotation1)) - position1.getY() * Math.sin(Math.toRadians(rotation1)),
                position1.getX() * Math.sin(Math.toRadians(rotation1)) + position1.getY() * Math.cos(Math.toRadians(rotation1))
        );
        Point coordinates2 = new Point(
                position2.getX() * Math.cos(Math.toRadians(rotation2)) - position2.getY() * Math.sin(Math.toRadians(rotation2)),
                position2.getX() * Math.sin(Math.toRadians(rotation2)) + position2.getY() * Math.cos(Math.toRadians(rotation2))
        );

        return Math.abs(coordinates1.y - coordinates2.y) <= 10 && Math.abs(coordinates1.x - coordinates2.x) <= margin;
    }

    /**
     * Method to check if the next device is to the right of the current device in the horizontal direction
     * To do this, we need to go back to the camera centered coordinate system
     * Be careful with conventions: the standard convention is ---> x and y upwards
     * The convention used in the camera centered coordinate system: <---- y and x downwards
     * @param position1 the position of the current device
     * @param position2 the position of the next device
     * @param margin number of centimeters the phones can deviate from each other in the y-direction (physical) == x-direction in camera-centered system
     * @return true if the devices are next to each other
     */
    public static boolean isNextInLineHorizontal(Position position1, Position position2, double margin){
        double rotation1 = position1.getRotation();
        double rotation2 = position2.getRotation();
        if(Math.abs(rotation1 - rotation2) >= 20)
            return false;

        //Rotate the position back to the coordinate system of the phone
        Point coordinates1 = new Point(
                position1.getX() * Math.cos(Math.toRadians(rotation1)) - position1.getY() * Math.sin(Math.toRadians(rotation1)),
                position1.getX() * Math.sin(Math.toRadians(rotation1)) + position1.getY() * Math.cos(Math.toRadians(rotation1))
        );
        Point coordinates2 = new Point(
                position2.getX() * Math.cos(Math.toRadians(rotation2)) - position2.getY() * Math.sin(Math.toRadians(rotation2)),
                position2.getX() * Math.sin(Math.toRadians(rotation2)) + position2.getY() * Math.cos(Math.toRadians(rotation2))
        );
        Log.d(TAG, "Position 1: " + coordinates1.x + ", " + coordinates1.y);
        Log.d(TAG, "Position 2: " + coordinates2.x + ", " + coordinates2.y);
        //Coordinates 2 needs to be to the right of coordinates1
        return Math.abs(coordinates1.x - coordinates2.x) <= margin && coordinates1.y - coordinates2.y > 0 && coordinates1.y - coordinates2.y <= 10;
    }

    /**
     * Method to check if the next device is to the right of the current device in the horizontal direction
     * To do this, we need to go back to the camera centered coordinate system
     * Be careful with conventions: the standard convention is ---> x and y upwards
     * The convention used in the camera centered coordinate system: <---- y and x downwards
     * @param device1 current device
     * @param device2 next device
     * @param margin number of centimeters the phones can deviate from each other in the y-direction (physical) == x-direction in camera-centered system
     * @return true if the devices are next to each other
     */
    public static boolean isNextInLineHorizontal(Device device1, Device device2, double margin){
        return isNextInLineHorizontal(device1.getPosition(), device2.getPosition(), margin);
    }

    public static boolean isNextInLineHorizontal(Position position1, Position position2){
        return isNextInLineHorizontal(position1, position2, MARGIN_NARROW);
    }

    /**
     * Method to check if the next device is below the current device
     * To do this, we need to go back to the camera centered coordinate system
     * Be careful with conventions: the standard convention is ---> x and y upwards
     * The convention used in the camera centered coordinate system: <---- y and x downwards
     * @param position1 the position of the current device
     * @param position2 the position of the next device
     * @param margin number of centimeters the phones can deviate from each other in the physical x-direction == y-direction in camera-centered system
     *               After some test, I noticed that this margin should be chosen broader, for example with margin = 1 cm, it won't give good results
     * @return true if the second device is below the current device
     */
    public static boolean isNextInLineVertical(Position position1, Position position2, int margin){
        double rotation1 = position1.getRotation();
        double rotation2 = position2.getRotation();
        if(Math.abs(rotation1 - rotation2) >= 20)
            return false;

        //Rotate the position back to the coordinate system of the phone
        Point coordinates1 = new Point(
                position1.getX() * Math.cos(Math.toRadians(rotation1)) - position1.getY() * Math.sin(Math.toRadians(rotation1)),
                position1.getX() * Math.sin(Math.toRadians(rotation1)) + position1.getY() * Math.cos(Math.toRadians(rotation1))
        );
        Point coordinates2 = new Point(
                position2.getX() * Math.cos(Math.toRadians(rotation2)) - position2.getY() * Math.sin(Math.toRadians(rotation2)),
                position2.getX() * Math.sin(Math.toRadians(rotation2)) + position2.getY() * Math.cos(Math.toRadians(rotation2))
        );
        Log.d(TAG, "Position 1: " + coordinates1.x + ", " + coordinates1.y);
        Log.d(TAG, "Position 2: " + coordinates2.x + ", " + coordinates2.y);
        //Assuming that coordinates1 needs to be above coordinates2:
        return Math.abs(coordinates1.y - coordinates2.y) <= margin && coordinates2.x - coordinates1.x > 0 && coordinates2.x - coordinates1.x <= 12;
    }

    /**
     * Method to check if the next device is below the current device
     * To do this, we need to go back to the camera centered coordinate system
     * Be careful with conventions: the standard convention is ---> x and y upwards
     * The convention used in the camera centered coordinate system: <---- y and x downwards
     * @param device1 the current device
     * @param device2 the next device
     * @param margin number of centimeters the phones can deviate from each other in the physical x-direction == y-direction in camera-centered system
     *               After some test, I noticed that this margin should be chosen broader, for example with margin = 1 cm, it won't give good results
     * @return true if the second device is below the current device
     */
    public static boolean isNextInLineVertical(Device device1, Device device2, int margin){
        return isNextInLineVertical(device1.getPosition(), device2.getPosition(), margin);
    }

    public static ArrayList<ArrayList<Device>> getGroups(ArrayList<Device> allDevices){
        ArrayList<ArrayList<Device>> allGroups = new ArrayList<>();
        int iteration = 0;
        for(Device device: allDevices){
            Log.d(TAG, "Device id " + device.getId());
            if(iteration == 0) {
                ArrayList<Device> group = new ArrayList<>();
                group.add(device);
                allGroups.add(group);
                iteration++;
            } else{
                boolean foundGroup = false;
                int numberOfGroups = 0;
                ArrayList<ArrayList<Device>> foundGroups = new ArrayList<>();
                for (ArrayList<Device> devices: allGroups) {
                    for(Device otherDevice: devices){
                        if(isNextTo(otherDevice, device)) {
                            devices.add(device);
                            foundGroup = true;
                            foundGroups.add(devices);
                            numberOfGroups++;
                        }

                    }
                }
                if(numberOfGroups > 1){
                    ArrayList<Device> mergeGroup = new ArrayList<>();
                    for(ArrayList<Device> otherGroup: foundGroups){
                        for (Device groupDevice: otherGroup) {
                            mergeGroup.add(groupDevice);
                        }
                        allGroups.remove(otherGroup);
                    }
                    allGroups.add(mergeGroup);
                }
                if(!foundGroup){
                    ArrayList<Device> newGroup = new ArrayList<>();
                    newGroup.add(device);
                    allGroups.add(newGroup);
                }
            }
        }
        return allGroups;
    }

    public static ArrayList<HashMap<String, Position>> getGroups(Map<String, Position> allDevices){
        ArrayList<HashMap<String, Position>> allGroups = new ArrayList<>();
        int iteration = 0;
        for(String key: allDevices.keySet()){
            if(iteration == 0){
                HashMap<String, Position> group = new HashMap<>();
                if(allDevices.get(key) != null && allDevices.get(key).getFoundPattern()) {
                    group.put(key, allDevices.get(key));
                    allGroups.add(group);
                    iteration++;
                }
            } else{
                boolean foundGroup = false;
                int numberOfGroups = 0;
                ArrayList<HashMap<String, Position>> foundGroups = new ArrayList<>();
                for(HashMap<String, Position> devices: allGroups){
                    try {
                        for (String mac : devices.keySet()) {
                            if(devices.get(mac) != null && allDevices.get(key) != null && devices.get(mac).getFoundPattern() && allDevices.get(key).getFoundPattern()) {
                                if (isNextToHorizontal(devices.get(mac), allDevices.get(key), 3)) {
                                    devices.put(key, allDevices.get(key));
                                    foundGroup = true;
                                    foundGroups.add(devices);
                                    numberOfGroups++;
                                }
                            }
                        }
                    }
                    catch(ConcurrentModificationException e){
                        e.printStackTrace();
                    }
                }
                if(numberOfGroups > 1){
                    HashMap<String, Position> mergeGroup = new HashMap<>();
                    for(HashMap<String, Position> otherGroup: foundGroups){
                        for(String macAddress: otherGroup.keySet())
                            mergeGroup.put(macAddress, otherGroup.get(macAddress));
                        allGroups.remove(otherGroup);
                    }
                    allGroups.add(mergeGroup);
                }
                if(!foundGroup){
                    HashMap<String, Position> newGroup = new HashMap<>();
                    newGroup.put(key, allDevices.get(key));
                    allGroups.add(newGroup);
                }
            }
        }
        return allGroups;
    }

    /**
     * Based on http://hotmath.com/hotmath_help/topics/line-of-best-fit.html
     */
    public static LinkedHashMap<String, Point> getLine(Map<String, Position> devices){
        //TODO: check for NaN
        Map<String, Point> devicesInRow = new TreeMap<>();
        Log.d(TAG, "Getting best line for " + devices.size() + " devices.");

        for(Map.Entry<String, Position> entry : devices.entrySet()){
            Log.d(TAG, "Device " + entry.getKey() + ", values " + entry.getValue());
        }

        //Remove devices without position
        Iterator it = devices.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            Position pos = (Position) entry.getValue();
            if(!pos.getFoundPattern()){
                it.remove();
                Log.e(TAG, "Device " + entry.getKey() + "'s pattern is not found.");
            }
        }

        double xAverage = 0.0;
        double yAverage = 0.0;
        double rotAverage = 0.0;
        for(Position position : devices.values()){
            xAverage += position.getX();
            yAverage += position.getY();
            rotAverage += position.getRotation();
        }
        xAverage /= devices.size();
        yAverage /= devices.size();
        rotAverage /= devices.size();

        Log.d(TAG, "xAverage[" + xAverage +"] yAverage[" + yAverage + "] rotAverage[" + rotAverage + "]");

        double numerator = 0.0;
        double denominator = 0.0;
        for(Position position : devices.values()){
            numerator += ( position.getX() - xAverage ) * ( position.getY() - yAverage );
            denominator += Math.pow((position.getX() - xAverage), 2);
        }

        double slope = numerator / denominator;

        double yIntercept = yAverage - slope * xAverage;

        final double A = slope;
        double B = -1;
        double C = yIntercept;
        Log.d(TAG, "Line: Ax + By + C = 0 -> " + A + "*x + " + B + "*y + " + C + " = 0");

        boolean angleUnderflow = false;
        boolean angleOverflow = false;
        double minAngle = rotAverage - ( ANGLE_TOLERATION / 2 );
        if(minAngle < 0) {
            minAngle += 360;
            angleUnderflow = true;
        }
        double maxAngle = rotAverage + ( ANGLE_TOLERATION / 2 );
        if(maxAngle > 360) {
            maxAngle -= 360;
            angleOverflow = true;
        }

        boolean invertOrder = false;
        if(Double.isNaN(A)){
            if(rotAverage < 90 && rotAverage > 270)
                invertOrder = true;
        }else{
            if(rotAverage < 180)
                invertOrder = true;
        }

        for(Map.Entry<String, Position> entry : devices.entrySet()){

            double rotation = entry.getValue().getRotation();
            boolean allowed = false;
            if(!angleUnderflow && !angleOverflow){
                if(rotation > minAngle && rotation < maxAngle){
                    allowed = true;
                }
            }else{
                if(rotation < maxAngle || rotation > minAngle){
                    allowed = true;
                }
            }
            if(allowed){
                if(Double.isNaN(A)){ //A (rico) is NaN (Infinity) - perpendicular to x-axis (not a function!)
                    if(Math.abs(entry.getValue().getX()) < DISTANCE_TOLERATION){ //TODO: change to entry.getValue().getX() - xAverage
                        devicesInRow.put(entry.getKey(), new Point(xAverage, entry.getValue().getY()));
                    }else{
                        Log.e(TAG, "Device " + entry.getKey() + " is out of line!");
                    }
                }else{ // A exists!
                    Log.d(TAG, "A is not NotANumber");
                    double distance = Math.abs(A * entry.getValue().getX() + B * entry.getValue().getY() + C)/Math.sqrt(Math.pow(A, 2) + Math.pow(B, 2));
                    //double distance = crossProduct(pointOne, pointTwo, new Point(entry.getValue().getX(), entry.getValue().getY())) / distance(pointOne, pointTwo);
                    if(distance < DISTANCE_TOLERATION){
                        Point pointOne = new Point(0, yIntercept);
                        Point pointTwo = new Point(1, yIntercept + slope);

                        //Store point on line
                        double apx = entry.getValue().getX() - pointOne.x;
                        double apy = entry.getValue().getY() - pointOne.y;
                        double abx = pointTwo.x - pointOne.x;
                        double aby = pointTwo.y - pointOne.y;

                        double ab2 = abx * abx + aby * aby;
                        double ap_ab = apx * abx + apy * aby;
                        double t = ap_ab / ab2;
                        Point point = new Point(pointOne.x + abx * t, pointOne.y + aby * t);
                        Log.d(TAG, "Point: " + point);
                        devicesInRow.put(entry.getKey(), point);
                    }else{
                        Log.e(TAG, "Device " + entry.getKey() + " is out of line!");
                    }
                }
            }else{
                Log.e(TAG, "Device " + entry.getKey() + " doesn't have the right orientation!");
            }
        }

        //Sort map TODO: filter map for DISTANCE_ON_AXIS_MIN and DISTANCE_ON_AXIS_MAX
        List<Map.Entry<String, Point>> entries = new LinkedList<>(devicesInRow.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Point>>() {
            @Override
            public int compare(Map.Entry<String, Point> o1, Map.Entry<String, Point> o2){
                Double returner;
                if(Double.isNaN(A)){ //parallel with y-axis - only y counts
                    returner = o1.getValue().y - o2.getValue().y;
                }else{
                    returner = o1.getValue().x - o2.getValue().x;
                }
                return returner.intValue();
            }
        });
        LinkedHashMap<String, Point> sortedMap = new LinkedHashMap<>();
        for(Map.Entry<String, Point> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        Log.d(TAG, "Sorted: " + sortedMap.toString());

        // Check that the distance between the devices is small enough
        Iterator iterator = sortedMap.entrySet().iterator();
        Map.Entry<String, Point> previousEntry = null;
        while(iterator.hasNext()){
            Map.Entry<String, Point> entry = (Map.Entry) iterator.next();
            if(previousEntry != null){
                if(getDistance(previousEntry.getValue(), entry.getValue()) > DISTANCE_BETWEEN_TOLERATION){
                    Log.d(TAG, "Distance toleration exceeded, returning empty list! " + getDistance(previousEntry.getValue(), entry.getValue()));
                    return new LinkedHashMap<>();
                }
            }
            previousEntry = entry;
        }

        // Optional inverting of order
        LinkedHashMap<String, Point> finalMap;
        if(invertOrder){
            finalMap = new LinkedHashMap<>();
            ListIterator<Map.Entry<String, Point>> iter = new ArrayList<>(sortedMap.entrySet()).listIterator(sortedMap.size());
            while(iter.hasPrevious()){
                Map.Entry<String, Point> entry = iter.previous();
                finalMap.put(entry.getKey(), entry.getValue());
            }
            Log.d(TAG, "Inverting order!");
        }else{
            finalMap = sortedMap;
        }

        Log.d("FinalMap", finalMap.toString());

        return finalMap;
    }

    /**
     * Compute the cross product AB x AC.
     * pointA and pointB are points on a line
     */
    private static double crossProduct(Point pointA, Point pointB, Point pointC){
        Point AB = new Point();
        Point AC = new Point();
        AB.x = pointB.x - pointA.x;
        AB.y = pointB.y - pointA.y;
        AC.x = pointC.x- pointA.x;
        AC.y = pointC.y - pointA.y;
        return AB.x * AC.y - AB.y * AC.x;
    }

    /**
     * Compute the distance between two points
     */
    private static double distance(Point pointA, Point pointB){
        double d1 = pointA.x - pointB.x;
        double d2 = pointA.y - pointB.y;

        return Math.sqrt(d1 * d2 + d2 * d2);
    }

}
