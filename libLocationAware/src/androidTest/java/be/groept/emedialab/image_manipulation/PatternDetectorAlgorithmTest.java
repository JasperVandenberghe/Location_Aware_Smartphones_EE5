package be.groept.emedialab.image_manipulation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import be.groept.emedialab.util.Tuple;

/**
 * Testclass for testing the PatternDetectorAlgorithm
 */
public class PatternDetectorAlgorithmTest extends AndroidTestCase {

    private final static String TAG = "AlgorithmTest";

    final CountDownLatch signal = new CountDownLatch(1);

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this.getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    signal.countDown();
                    break;
            }
        }
    };

    /*@Override
    protected void setUp() throws Exception{

        if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this.getContext(), mOpenCVCallBack)){
            Log.e(TAG, "Error opening OpenCV, tests can't be run.");
        }

        signal.await(10, TimeUnit.SECONDS);

    }*/

    /**
     * One function to call all the tests to prevent OpenCV needing to load for every test because this takes some time.
     * Downside: tests do not run in random order which is not good.
     * TODO: If enough time, look for another way to do these tests in random order
     */
    //@Test
    public void tests(){
        findEmptyPattern();
        findPatternInCentre();
        findPatternOffsetTopLeft();
        testGetAveragePatten();
    }

    public void findEmptyPattern(){
        String filename = "pattern_none_0x0_640x480.png";

        //assertEquals(false, imageToPattern(filename).getPatternFound());
    }

    /**
     * Checks if the pattern is in the centre of the display and found
     */
    public void findPatternInCentre() {
        String filename = "pattern_centered_100x100_640x480.png";

        PatternCoordinates expectedPattern = new PatternCoordinates(
                new Point(369, -289),
                new Point(270, -289),
                new Point(270, -190),
                new Point(369, -190),
                0
        );
        /*PatternCoordinates pattern = imageToPattern(filename);

        assertEquals(true, pattern.getPatternFound());

        //assertEquals(expectedPattern.toString(), pattern.toString());

        //Only check corners because angle doesn't matter
        for(int i = 1; i <= 4; i++){
            assertEquals(expectedPattern.getNum(i), pattern.getNum(i));
        }*/
    }

    public void findPatternOffsetTopLeft() {
        String filename = "pattern_offset_top_left_100x100_60x480.png";

        PatternCoordinates expectedPattern = new PatternCoordinates(
                new Point(319, -239),
                new Point(220, -239),
                new Point(220, -140),
                new Point(319, -140),
                0
        );
        /*PatternCoordinates pattern = imageToPattern(filename).element1;

        assertEquals(true, pattern.getPatternFound());

        // Only check corners because angle doesn't matter
        for(int i = 1; i <= 4; i++){
            assertEquals(expectedPattern.getNum(i), pattern.getNum(i));
        }*/
    }

    private Tuple<PatternCoordinates, Mat> imageToPattern(String filename){
        PatternDetectorAlgorithm patternDetectorAlgorithm = new PatternDetectorAlgorithm();

        Mat rgba = createMat(filename);
        Mat gray = new Mat();

        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGB2GRAY);

        return patternDetectorAlgorithm.find(rgba, gray);
    }

    /**
     * Creates the matrix of a given file form the assets by moving it to the local filestorage
     * @param filename the name of the file in the assets folder
     * @return a Mat with the color values of the image of which the filename was provided
     */
    private Mat createMat(String filename){
        try {
            FileOutputStream fos = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            InputStream file = getContext().getAssets().open(filename);
            Bitmap image = BitmapFactory.decodeStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        File file = new File(getContext().getFilesDir() + "/" + filename);

        return Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
    }

    /**
     * Tests whether getAveragePattern() returns null or the average of all the values or not.
     */
    public void testGetAveragePatten(){
        ArrayList<PatternCoordinates> coordinates = new ArrayList<>();
        assertEquals(null, PatternDetectorAlgorithm.getAveragePattern(coordinates));

        PatternCoordinates firstPattern = new PatternCoordinates(new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0), 0);
        coordinates.add(firstPattern);

        assertEquals(firstPattern, PatternDetectorAlgorithm.getAveragePattern(coordinates));

        coordinates.add(new PatternCoordinates(new Point(10, 10), new Point(10, 10), new Point(10, 10), new Point(10, 10), 10));
        assertEquals(new PatternCoordinates(new Point(5, 5), new Point(5, 5), new Point(5, 5), new Point(5, 5), 5), PatternDetectorAlgorithm.getAveragePattern(coordinates));
    }
}