package be.groept.emedialab.image_manipulation;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import be.groept.emedialab.util.GlobalResources;
import be.groept.emedialab.util.Tuple;

/**
 * The new implementation of the pattern detection. This implementation does not only check for
 * shapes and their size ratio, but also whether the shapes have four corners (using the Ramer-
 * Douglas-Peucker algorithm) and several other improvements over the old algorithm.
 */
public class PatternDetectorAlgorithm implements PatternDetectorAlgorithmInterface{

    private static final double epsilon = 0.10;
    private static final int amountBeforePatternLost = 5;
    private final ArrayList<PatternCoordinates> patternList = new ArrayList<>();
    private final PatternCoordinates noPatternFoundPattern = new PatternCoordinates(
            new Point(0, 0),
            new Point(0, 0),
            new Point(0, 0),
            new Point(0, 0),
            0.0,
            false
    );

    private int amountToAverage = 5;
    private int amountOfFramesWithoutPattern = 0;
    private Mat backgroundMatrix = new Mat();

    private final Scalar orange = new Scalar(255, 120, 0);
    private final Scalar light_blue = new Scalar(0, 255, 255);
    private final Scalar dark_blue = new Scalar(0, 120, 255);
    private final Scalar light_green = new Scalar(0, 255, 0);
    private final Scalar dark_green = new Scalar(0, 100, 0);
    private final Scalar dark_red = new Scalar(255, 0, 0);

    public PatternDetectorAlgorithm(){}

    public PatternDetectorAlgorithm(int amountToAverage){
        this.amountToAverage = amountToAverage;
    }

    public Tuple<PatternCoordinates, Mat> find(Mat backgroundMatrix, Mat binaryMatrix) {
        return find(backgroundMatrix, binaryMatrix, false);
    }

    /**
     * Find the pattern!
     *
     * <img src="./doc-files/PatternDetection_01.png" alt="Pattern Detection 01"/>
     *
     * The figure below shows the order in which the corner points of the pattern are defined.
     * Point 1 is near the inner square. Then moving in the clockwise direction you find point 2, 3 and 4.
     *
     * <img src="./doc-files/Pattern_Point_Naming_Convention.png" alt="Naming Convention for Points of Pattern"/>
     *
     * @param backgroundMatrix Color image
     * @param binaryMatrix Grey scale image
     * @return Corner points and angle of the pattern.
     *         Careful x and y axis are corrected to be compatible with Calc.java convention!
     */
    public Tuple<PatternCoordinates, Mat> find(Mat backgroundMatrix, Mat binaryMatrix, boolean convert) {

        if(convert){
            Mat temp = new Mat();
            Imgproc.cvtColor(backgroundMatrix, temp, Imgproc.COLOR_GRAY2RGB);
            this.backgroundMatrix = temp;
        }else{
            this.backgroundMatrix = backgroundMatrix;
        }

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> bigContours;
        List<MatOfPoint> bigSquareContours;

        // Define a default return value for when things should go terribly wrong.
        PatternCoordinates detectedPattern = null;

        // Find contours in image
        Imgproc.findContours(binaryMatrix, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_CONTOURS)){
            Imgproc.drawContours(this.backgroundMatrix, contours, -1, orange, 4);
        }

        // Filter out too small contours
        bigContours = getBigContours(contours);

        if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_BIG_CONTOURS)){
            Imgproc.drawContours(this.backgroundMatrix, bigContours, -1, orange, 4);
        }

        // Get all square contours
        bigSquareContours = getSquareContours(bigContours);

        if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_SQUARE_BIG_CONTOURS)){
            Imgproc.drawContours(this.backgroundMatrix, bigSquareContours, -1, orange, 4);
        }

        // Return error if device is tilted
        if(GlobalResources.getInstance().getTilted()) {
            putText("Device tilted!", new Point(50, 250), light_blue);
            return new Tuple<>(noPatternFoundPattern, this.backgroundMatrix);
        }

        // Get the actual pattern (takes smallest pattern if multiple are found)
        double smallestOuterSquare = Double.POSITIVE_INFINITY;
        MatOfPoint outerRect = null;
        RotatedRect outerRotatedRect = null;
        RotatedRect innerRotatedRect = null;
        for(MatOfPoint outerContour : bigSquareContours){
            for(MatOfPoint innerContour : bigSquareContours){
                // Filter out comparing the same contour
                if(!innerContour.equals(outerContour)){
                    // Check if the outer contour is bigger than the inner one and if it is the smallest one
                    if(Imgproc.contourArea(outerContour) > Imgproc.contourArea(innerContour) && Imgproc.contourArea(outerContour) < smallestOuterSquare){
                        // Convert the contour to Point2f
                        MatOfPoint2f mMOP2fOuter = new MatOfPoint2f();
                        outerContour.convertTo(mMOP2fOuter, CvType.CV_32FC2);
                        MatOfPoint2f mMOP2fInner = new MatOfPoint2f();
                        innerContour.convertTo(mMOP2fInner, CvType.CV_32FC2);

                        // Get the smallest bounding rectangle
                        RotatedRect rotatedOuter = Imgproc.minAreaRect(mMOP2fOuter);
                        RotatedRect rotatedInner = Imgproc.minAreaRect(mMOP2fInner);

                        // Check the ratio is proportional
                        // double ratio = rotatedOuter.size.area()/rotatedInner.size.area();
                        double ratio = Imgproc.contourArea(outerContour) / Imgproc.contourArea(innerContour);
                        if((ratio > 4) && (ratio < 16)){
                            // Check if the inner square is inside the outer square
                            if(rotatedInner.center.inside(rotatedOuter.boundingRect())){
                                outerRect = outerContour;
                                outerRotatedRect = rotatedOuter;
                                innerRotatedRect = rotatedInner;
                                smallestOuterSquare = Imgproc.contourArea(outerContour);
                            }
                        }
                    }
                }
            }
        }

        // If pattern is found, draw it and reorder the outer points
        if(outerRect != null){
            if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_PATTERN)) {
                drawRect(outerRotatedRect, dark_blue);
                drawRect(innerRotatedRect, light_green);
            }

            // Get the points of the rectangle
            Point out[] = outerRect.toArray();

            detectedPattern = reorderPoints(out, innerRotatedRect.center);
        }

        // If no pattern found
        if(detectedPattern == null){
            if(amountOfFramesWithoutPattern >= amountBeforePatternLost - 1){ // Too many frames without pattern
                // Reset list and add the default pattern
                patternList.clear();
                return new Tuple<>(noPatternFoundPattern, this.backgroundMatrix);
            }else{ // Less than x frames without pattern
                if(patternList.size() > 0) {
                    detectedPattern = patternList.get(patternList.size() - 1);
                }else{
                    return new Tuple<>(noPatternFoundPattern, this.backgroundMatrix);
                }
                amountOfFramesWithoutPattern++;
            }
        }else{
            // Reset amount of frames without a pattern
            amountOfFramesWithoutPattern = 0;
        }

        if(patternList.size() == amountToAverage){
            patternList.remove(0);
        }
        if(detectedPattern != null)
            patternList.add(detectedPattern);

        if(GlobalResources.getInstance().getMoving()){
            //If moving, clear average and take current sample
            patternList.clear();
            if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_PATTERN))
                drawPattern(detectedPattern, dark_green);
        }else{
            //If not moving, take average over previous x samples
            detectedPattern = getAveragePattern(patternList);
            if(GlobalResources.getInstance().getImageSettings().getOverlayEnabled(ImageSettings.OVERLAY_PATTERN))
                drawPattern(detectedPattern, dark_red);
        }
        return new Tuple<>(detectedPattern, this.backgroundMatrix);
    }

    /**
     * Calculates the average pattern of an ArrayList of patterns.
     * @param allCoordinates a list of all patterns that needs to be averaged.
     * @return The average value of all patterns inside the list.
     */
    public static PatternCoordinates getAveragePattern(List<PatternCoordinates> allCoordinates){

        //If no coordinates, return null
        if(allCoordinates.size() == 0){
            return null;
        }

        //Set first coordinate as base
        PatternCoordinates finalCoordinates = new PatternCoordinates(
                new Point(allCoordinates.get(0).getNum(1).x, allCoordinates.get(0).getNum(1).y),
                new Point(allCoordinates.get(0).getNum(2).x, allCoordinates.get(0).getNum(2).y),
                new Point(allCoordinates.get(0).getNum(3).x, allCoordinates.get(0).getNum(3).y),
                new Point(allCoordinates.get(0).getNum(4).x, allCoordinates.get(0).getNum(4).y),
                allCoordinates.get(0).getAngle());

        //For all next coordinates in array add
        for(int i = 1; i < allCoordinates.size(); i++){

            //Update corners
            for(int ii = 1; ii <= 4; ii++){
                finalCoordinates.getNum(ii).x += allCoordinates.get(i).getNum(ii).x;
                finalCoordinates.getNum(ii).y += allCoordinates.get(i).getNum(ii).y;
            }

            //Update angle
            finalCoordinates.setAngle(finalCoordinates.getAngle() + allCoordinates.get(i).getAngle());
        }

        //Divide finalCoordinate by amount of coordinates
        for(int i = 1; i <= 4; i++){
            finalCoordinates.getNum(i).x /= allCoordinates.size();
            finalCoordinates.getNum(i).y /= allCoordinates.size();
        }
        finalCoordinates.setAngle(finalCoordinates.getAngle() / allCoordinates.size());

        return finalCoordinates;
    }

    /**
     * Draws a rectangle in the provided color onto the backgroundMatrix of the object.
     * @param rotatedRect The rectangle that needs to be drawn.
     * @param color The color the rectangle needs to be displayed in.
     */
    private void drawRect(RotatedRect rotatedRect, Scalar color){
        Point a = new Point(rotatedRect.boundingRect().x, rotatedRect.boundingRect().y);
        Point b = new Point(rotatedRect.boundingRect().x + rotatedRect.boundingRect().width, rotatedRect.boundingRect().y + rotatedRect.boundingRect().height);
        Core.rectangle(this.backgroundMatrix, a, b, color, 3);
    }

    /**
     * Writes some text in a color on a certain point on the backgroundMatrix of the object.
     * @param text The text that needs to be displayed.
     * @param point The point on the canvas where the text should be printed.
     * @param color The color in which the text must be printed.
     */
    private void putText(String text, Point point, Scalar color){
        Core.putText(this.backgroundMatrix, text, point, Core.FONT_HERSHEY_SIMPLEX, 1, color);
    }

    /**
     * THis draws a pattern square on the canvas in the provided color.
     * @param pattern The pattern that needs to be drawn. This is actually just a rectangle,
     *                represented by four corners.
     * @param color The color in which it needs to be displayed.
     */
    private void drawPattern(PatternCoordinates pattern, Scalar color){
        Core.line(this.backgroundMatrix, pattern.getNum(1), pattern.getNum(2), color, 3);
        Core.line(this.backgroundMatrix, pattern.getNum(2), pattern.getNum(3), color, 3);
        Core.line(this.backgroundMatrix, pattern.getNum(3), pattern.getNum(4), color, 3);
        Core.line(this.backgroundMatrix, pattern.getNum(4), pattern.getNum(1), color, 3);
    }

    /**
     * This method reorders the points of the patern in the correct order per convention.
     *
     * @param point List of points representing the points of the pattern.
     * @param in_center Center of the small inner white square.
     * @return The points of the pattern in the right order.
     *          The angle is this patternCoordinator object is to be interpreted
     *          as the distance between Point 1 and the white square.
     */
    private PatternCoordinates reorderPoints(Point[] point, Point in_center){
        Point[] point_send = new Point[4];
        double distance = 0; //Distance from Point to Inner Center.
        double min_dis = Double.POSITIVE_INFINITY; //Initialize at infinity.

        for(int i = 0; i < 4; i++){
            distance = Math.sqrt( Math.pow(in_center.x - point[i].x, 2) + Math.pow(in_center.y - point[i].y, 2) );
            if(distance < min_dis){
                // If the current point is closer to the white square then rearrange the points
                // so that the current point is the first point.
                min_dis = distance;

                point_send[0] = point[i]; // Place the current point first.
                // Increase index each time to move clockwise through the points. Use modulo 4 so that after three comes zero. (4/4 = 1 -> Remainder = 0)
                point_send[1] = point[ ( i + 1 ) % 4];
                point_send[2] = point[ ( i + 2 ) % 4];
                point_send[3] = point[ ( i + 3 ) % 4];
                
            }
        }
        return new PatternCoordinates(point_send[0], point_send[1], point_send[2], point_send[3], distance);
    }

    /**
     * Find contours that have 4 points
     * Based on http://opencv-code.com/tutorials/detecting-simple-shapes-in-an-image/
     *
     * @param contours contours to search through
     * @return list of square contours
     */
    private List<MatOfPoint> getSquareContours(List<MatOfPoint> contours) {
        List<MatOfPoint> squareContours = new ArrayList<>();
        MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
        MatOfPoint2f approx = new MatOfPoint2f();
        for(int i = 0; i < contours.size(); i++){

            // Convert MatOfPoint to MatOfPoint2f
            contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);

            // Approximate contour with a the precision Epsilon, last parameter is "closed"
            // Uses https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm, see http://karthaus.nl/rdp/ for example
            Imgproc.approxPolyDP(mMOP2f1, approx, Imgproc.arcLength(mMOP2f1, true) * epsilon, true);

            if(approx.size().equals(new Size(1, 4))){
                squareContours.add(new MatOfPoint(approx.toArray()));
            }
        }

        return squareContours;
    }

    /**
     * Filters out the contours that have too small of an surface area.
     * @param contours The contours that need to be filtered.
     * @return The new list of big contours.
     */
    private List<MatOfPoint> getBigContours(List<MatOfPoint> contours){
        List<MatOfPoint> bigContours = new ArrayList<>();
        for(MatOfPoint contour : contours){
            if(Imgproc.contourArea(contour) > 20){
                bigContours.add(contour);
            }
        }
        return bigContours;
    }
}
