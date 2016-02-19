package be.groept.emedialab.image_manipulation;

import android.content.Context;
import android.util.Log;

import be.groept.emedialab.math.PositionCalculation;
import be.groept.emedialab.movement.MovementAccelerometer;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;
import be.groept.emedialab.util.Point3D;
import be.groept.emedialab.util.Tuple;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Grabs frames from the camera and gives them to the Pattern Detection Algorithm.
 * The results are stored in the {@link GlobalResources} Singleton.
 *
 * The constructor can be called at any time. The method {@link #setup()} has to be called after the OpenCV library has loaded.
 * Use {@link #destroy()} to clean up.
 *
 *
 * @see PatternDetectorAlgorithmInterface
 */
public class PatternDetector {

    private static final String TAG = "PatternDetector";
    private static final long sampleRate = 200;
    private final static boolean DEBUG = false;

    private PositionCalculation calc;
    private VideoCapture mCamera;
    private PatternDetectorAlgorithmInterface patternDetectorAlgorithm;
    private boolean isPaused = false;
    private ArrayList<Long> averageTime = new ArrayList<>();

    /**
     * Indicates which camera should be used.
     * '0' = back facing camera.
     * '1' = front facing camera.
     */
    private int camera = 1;

    private Runnable cameraRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mCamera != null) {
                    long startTime;
                    if(DEBUG)
                        startTime = System.currentTimeMillis();
                    if (mCamera.grab()){
                        Mat rgba = new Mat();
                        Mat gray = new Mat();
                        Mat binary = new Mat();
                        mCamera.retrieve(rgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);

                        if(camera == 1) {
                            //Flip the image around the openCv x-axis (== Calc y-axis) if the front facing camera is used.
                            //See: http://answers.opencv.org/question/8804/ipad-camera-input-is-rotated-180-degrees/
                            Core.flip(rgba, rgba, 1);
                            Core.flip(rgba, rgba, 0);
                        }

                        // Convert to grey-scale.
                        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);

                        // Threshold the grey-scale to binary
                        Imgproc.threshold(gray, binary, 80, 255, Imgproc.THRESH_BINARY);

                        Tuple<PatternCoordinates, Mat> patternAndImagePair = null;
                        switch(GlobalResources.getInstance().getImageSettings().getBackgroundMode()){
                            case ImageSettings.BACKGROUND_MODE_RGB:
                                patternAndImagePair = patternDetectorAlgorithm.find(rgba, binary, false);
                                break;
                            case ImageSettings.BACKGROUND_MODE_GRAYSCALE:
                                patternAndImagePair = patternDetectorAlgorithm.find(gray, binary, true);
                                break;
                            case ImageSettings.BACKGROUND_MODE_BINARY:
                                patternAndImagePair = patternDetectorAlgorithm.find(binary, binary, true);
                        }
                        GlobalResources.getInstance().updateImage(patternAndImagePair.element2);

                        calculateCoordinates(patternAndImagePair.element1);
                        if(DEBUG){
                            long thisTime = (System.currentTimeMillis() - startTime);
                            if(thisTime < 300){
                                averageTime.add(thisTime);
                                long average = 0;
                                for(long time : averageTime){
                                    average += time;
                                }
                                average /= averageTime.size();
                                Log.d(TAG, "Average pattern recognition time: " + average + ", current interation time: " + thisTime);
                            }else{
                                Log.e(TAG, "Too long thisTime! " + thisTime);
                            }
                        }
                        System.gc();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public PatternDetector(int camera, boolean newAlgorithm) {
        if(newAlgorithm){
            patternDetectorAlgorithm = new PatternDetectorAlgorithm(5);
        }else{
            patternDetectorAlgorithm = new PatternDetectorAlgorithmOld();
        }
        setCamera(camera);
    }

    public PatternDetector(int camera, boolean newAlgorithm, Context mContext) {
        this(camera, newAlgorithm);
        // TODO: currently this object lives here, but it shouldn't. It should be in a runnable? Not a runnable, runnable gets executed at fixed time intervals
        new MovementAccelerometer(mContext);
    }

    private void calculateCoordinates(PatternCoordinates patternCoordinates) {
        Point3D deviceCoordinates = calc.patternToReal(patternCoordinates); //Calculate the position of this device.
        Position devicePosition = new Position(deviceCoordinates.getX(), deviceCoordinates.getY(), deviceCoordinates.getZ(), calc.calculateRotation(patternCoordinates));
        devicePosition.setFoundPattern(patternCoordinates.getPatternFound());
        GlobalResources.getInstance().updateOwnPosition(devicePosition);
    }

    /**
     * Load camera with the {@link VideoCapture} class.
     * Code is based on <a href="http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/get-started-with-opencv-on-android/">http://developer.sonymobile.com/knowledge-base/tutorials/android_tutorial/get-started-with-opencv-on-android/</a>
     */
    public void setup() {

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        mCamera = new VideoCapture(camera);
        isPaused = false;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(cameraRunnable, 0, sampleRate, TimeUnit.MILLISECONDS);
    }

    /**
     * Release Camera and cleanup.
     */
    public void destroy(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
        isPaused = true;
        // TODO stop executorService
    }

    public boolean isPaused(){
        return isPaused;
    }

    public void setCalc(PositionCalculation calc) {
        this.calc = calc;
    }

    public int getCamera() {
        return camera;
    }

    private void setCamera(int camera) {
        if(camera != 0 && camera != 1){
            throw new IllegalArgumentException("Invalid value: camera should be 0 or 1.");
        }else{
            this.camera = camera;
        }
    }
}
