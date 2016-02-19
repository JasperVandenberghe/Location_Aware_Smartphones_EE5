package be.groept.emedialab.image_manipulation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import be.groept.emedialab.math.PositionCalculation;
import be.groept.emedialab.util.GlobalResources;

/**
 * Class to set up the pattern detector when game is started
 */
public class RunPatternDetector {

    private Activity activity;

    public RunPatternDetector(Activity activity){
        this.activity = activity;
        PatternDetector patternDetector = GlobalResources.getInstance().getPatternDetector();
        if(patternDetector != null)
            patternDetector.setup();
        BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        onOpenCVSuccessLoad();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, activity, baseLoaderCallback);
    }

    private void onOpenCVSuccessLoad(){
        if(GlobalResources.getInstance().getPatternDetector() == null)
            setupPatternDetector();
    }

    @SuppressWarnings("deprecation")
    private void setupPatternDetector(){
        PatternDetector patternDetector = null;
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
            double patternWidthCm = Double.parseDouble(sharedPref.getString("pattern_size", "18"));
            boolean newAlgorithm = sharedPref.getBoolean("new_algorithm", true);

            //Select camera
            //If there is no front facing camera, use back camera
            int cameraSelection = (Camera.getNumberOfCameras() < 2) ? 0 : 1;

            Camera camera = Camera.open(cameraSelection);
            Camera.Parameters params = camera.getParameters();
            Camera.Size imageSize = params.getPictureSize();
            camera.release();

            PositionCalculation positionCalculation = new PositionCalculation(patternWidthCm, imageSize.width, imageSize.height, params.getHorizontalViewAngle());

            patternDetector = new PatternDetector(cameraSelection, newAlgorithm, activity.getApplicationContext());
            patternDetector.setCalc(positionCalculation);
            GlobalResources.getInstance().setPatternDetector(patternDetector);
        } catch (RuntimeException e){
            e.printStackTrace();
        }
        setupCamera(patternDetector);
    }

    private void setupCamera(PatternDetector patternDetector){
        if(patternDetector != null){
            patternDetector.setup();
        }
    }
}
