package be.groept.emedialab.image_manipulation;

import org.opencv.core.Mat;

import be.groept.emedialab.util.Tuple;

public interface PatternDetectorAlgorithmInterface {

    /*
     * @param rgba Color image
     * @param gray2 Grayscale image
     * @return Corner points and angle of the pattern.
     */
    Tuple<PatternCoordinates, Mat> find(Mat backgroundMatrix, Mat binaryMatrix, boolean convert);
}
