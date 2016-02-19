package be.groept.emedialab.math;

import android.util.Log;

/**
 * Singleton to initialize the camera constants so they can be used in the Calc class
 */
public class CameraConstants {

    private static CameraConstants instance = null;

    //Value in pixels
    private double ex;
    //Value in pixels
    private double ey;
    //Value in cm
    private double height;
    //ID on the back of the phone
    private int id;

    private int counter = -30;
    private int cameraFrames = 0;
    private int maxCameraFrames = 5;

    private CameraConstants(){

    }

    public synchronized static CameraConstants getInstance(){
        if(instance == null)
            instance = new CameraConstants();
        return instance;
    }

    public void initPhone(String phoneId){
        switch (phoneId){
            case "867545010624225": //Phone 1
                id = 1;
                ex = -6.9125;
                ey = 10.4875;
                height = 218.50927741563095;
                break;
            case "867545010631055": //Phone 2
                id = 2;
                ex = 23.4300;
                ey = 19.7925;
                height = 219.23791744004117;
                break;
            case "867545010618169": //Phone 3
                id = 3;
                ex = -7.0375;
                ey = 4.1500;
                height = 218.21679590277623;
                break;
            case "867545010628416": //Phone 4
                id = 4;
                ex = 19.3700;
                ey = -19.4300;
                height = 215.69303130245342;
                break;
            case "867545010629539": //Phone 5
                id = 5;
                ex = -7.1075;
                ey = -5.5800;
                height = 218.234659250652;
                break;
            default:
                id = 0;
                ex = 0;
                ey = 0;
                height = 0;
                break;
        }
    }

    public double getEx(){
        return ex;
    }

    public double getEy(){
        /*cameraFrames++;
        if(cameraFrames > maxCameraFrames){
            counter++;
            if(counter > 30){
                counter = 0;
            }
            Log.d("Error signal", "ey " + counter);
            cameraFrames = 0;
        }
        return counter;*/
        return ey;
    }

    public double getHeight(){
        return height;
    }

    public int getId(){
        return id;
    }
}
