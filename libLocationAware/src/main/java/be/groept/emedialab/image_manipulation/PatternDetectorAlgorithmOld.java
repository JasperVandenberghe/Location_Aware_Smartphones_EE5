package be.groept.emedialab.image_manipulation;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import be.groept.emedialab.util.Tuple;

/**
 * This is the `old` pattern detection algorithm and has not been re-tested with the renewal of
 * other components of the library. There is a high possibility certain parts will not work.
 */
public class PatternDetectorAlgorithmOld implements PatternDetectorAlgorithmInterface{
    private int ii = 0; //What is ii?
    private int distance;
    private int distance2;
    private boolean setupflag;

    private Scalar orange = new Scalar(255, 120, 0); //Potential
    private Scalar light_blue = new Scalar(0, 255, 255);
    private Scalar dark_blue = new Scalar(0, 120, 255);
    private Scalar light_green = new Scalar(0, 255, 0);

    public PatternDetectorAlgorithmOld(){
        distance2 = 0;
        setupflag = false;
    }

    /**
     * Do magic!
     *
     * <img src="./doc-files/PatternDetection_01.png" alt="Pattern Detection 01"/>
     *
     * The figure below shows the order in which the corner points of the pattern are defined.
     * Point 1 is near the inner square. Then moving in the clockwise direction you find point 2, 3 and 4.
     *
     * <img src="./doc-files/Pattern_Point_Naming_Convention.png" alt="Naming Convention for Points of Pattern"/>
     *
     * (Uses bitwise operators instead of logical operators. Bitwise is marginally faster.)
     *
     * @param rgba Color image
     * @param gray2 Grayscale image
     * @return Corner points and angle of the pattern.
     *         Careful x and y axis are corrected to be compatible with Calc.java convention!!!!!!
     */
    public Tuple<PatternCoordinates, Mat> find(Mat rgba, Mat gray2, boolean unused) {
        //Start timing
        long startTime = System.currentTimeMillis();

        List<MatOfPoint> contour = new ArrayList<MatOfPoint>(); //List of all the contours

        Mat mIntermediateMat = new Mat();

        List<MatOfPoint> con_in_range;
        List<MatOfPoint> squareContours;
        List<MatOfPoint> pContour;

        MatOfPoint squ_in  = new MatOfPoint();
        MatOfPoint squ_out = new MatOfPoint();

        //Define a default return value for when things should go terribly wrong.
        PatternCoordinates detectedPattern  = new PatternCoordinates(
                //Pattern in center.
                new Point(320-50,240-50),
                new Point(320+50,240-50),
                new Point(320-50,240+50),
                new Point(320+50,240+50),
                0.00
        );

        //Apply tresholding, result is stored in 'mIntermediateMat'
        Imgproc.threshold(gray2, mIntermediateMat, 80, 255, Imgproc.THRESH_BINARY);
        //Let OpenCV find contours, the result of this operation is stored in 'contour'.
        Imgproc.findContours(mIntermediateMat, contour, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        //Filter out contours with the wrong size.
        con_in_range = getContoursBySize(distance2, contour);
        //Filter out non square contours.
        squareContours = getContoursSquare2(con_in_range);
        //Find the right contour for the pattern.
        pContour = findPattern(squareContours);

        //Overlay the image with some useful lines.
        //Here we draw all potential patterns in orange.
        Imgproc.drawContours(rgba, squareContours, -1, orange, 4);

        if(setupflag == false)
        {
            if(pContour.size() == 2) {
                squ_out = pContour.get(1);
                squ_in = pContour.get(0);

                RotatedRect rot_re_out = new RotatedRect();
                RotatedRect rot_re_in = new RotatedRect();

                MatOfPoint2f mp2f_out = new MatOfPoint2f();
                MatOfPoint2f mp2f_in = new MatOfPoint2f();

                mp2f_in = new MatOfPoint2f(squ_in.toArray());
                mp2f_out = new MatOfPoint2f(squ_out.toArray());

                rot_re_out = Imgproc.minAreaRect(mp2f_out);
                rot_re_in = Imgproc.minAreaRect(mp2f_in);

                double size_out = rot_re_out.size.area(); //Area of the outer black square
                double size_in = rot_re_in.size.area(); //Area of the inner white square

                double ratio = size_out/size_in;
                if((ratio>4) && (ratio<16)){ //Check if the ratio between outer and inner square area is correct.
                    ii++;
                    if(ii == 3) {
                        setupflag = true;
                        distance2 = distance2 + 4;
                        ii = 0;
                    }
                }
            }

            if(distance2 > 50){
                distance2 = 0;
            }
            distance2 = distance2 + 2;
        }
        else {
            Point innerCenter = new Point();
            Point outterCenter = new Point();

            RotatedRect NewMtx1 = new RotatedRect();
            RotatedRect NewMtx2 = new RotatedRect();

            MatOfPoint2f appo = new MatOfPoint2f();
            MatOfPoint2f appo2 = new MatOfPoint2f();
            if (pContour.size() == 2) {
                appo = new MatOfPoint2f(pContour.get(0).toArray());
                NewMtx1 = Imgproc.minAreaRect(appo);
                innerCenter = NewMtx1.center;
                appo2 = new MatOfPoint2f(pContour.get(1).toArray());
                NewMtx2 = Imgproc.minAreaRect(appo2);
                outterCenter = NewMtx2.center;
            }
            else{
                //setupflag = false;
            }

            List<MatOfPoint> appro_con = new ArrayList<MatOfPoint>();
            appro_con.add(new MatOfPoint(appo.toArray()));

            //Outer square
            Point a = new Point(NewMtx2.boundingRect().x, NewMtx2.boundingRect().y);
            Point b = new Point(NewMtx2.boundingRect().x + NewMtx2.boundingRect().width, NewMtx2.boundingRect().y + NewMtx2.boundingRect().height);
            Core.rectangle(rgba, a, b, dark_blue, 3);

            Point out[] = new Point[4];
            NewMtx2.points(out);
            PatternCoordinates out_send = Cal_Pointnum(out, innerCenter);

            if(out_send.getNum(2) !=  null) {
                String out_point1 = "1";//"point 1 is ("+ String.valueOf(out[0].x)+","+ String.valueOf(out[0].y)+")";
                String out_point2 = "2";//"point 2 is ("+ String.valueOf(out[1].x)+","+ String.valueOf(out[1].y)+")";
                String out_point3 = "3";//"point 3 is ("+ String.valueOf(out[2].x)+","+ String.valueOf(out[2].y)+")";
                String out_point4 = "4";//"point 4 is ("+ String.valueOf(out[3].x)+","+ String.valueOf(out[3].y)+")";
                Core.putText(rgba, out_point1, out_send.getNum(1), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
                Core.putText(rgba, out_point2, out_send.getNum(2), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
                Core.putText(rgba, out_point3, out_send.getNum(3), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
                Core.putText(rgba, out_point4, out_send.getNum(4), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
                //Core.putText(image, String.valueOf(out_send.getAngle()),new Point(50, 300) , Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
            }

            //Inner square
            Point a2 = new Point(NewMtx1.boundingRect().x, NewMtx1.boundingRect().y);
            Point b2 = new Point(NewMtx1.boundingRect().x + NewMtx1.boundingRect().width, NewMtx1.boundingRect().y + NewMtx1.boundingRect().height);
            Core.rectangle(rgba, a2, b2, light_green,3);

            //Angle
            double extra_angle = calculateExtraAngle(innerCenter.x, innerCenter.y, outterCenter.x, outterCenter.y);
            double finalangle = NewMtx2.angle+90+extra_angle;

            //String kk = "k is (" + String.valueOf(k) + ")";
            //Core.putText(rgba, kk, new Point(50, 400), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
            String dis = "the distance2 is "+ String.valueOf(distance2) +" )";
            Core.putText(rgba, dis, new Point(50, 350), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);
            String angle = "rotate angle is (" + String.valueOf(finalangle) + ")";
            Core.putText(rgba, angle, new Point(50, 450), Core.FONT_HERSHEY_SIMPLEX, 1, light_blue);

            long stopTime = System.currentTimeMillis();
            String elapsedTime = String.valueOf(stopTime - startTime);
            //Log.i("Algorithm", "Spend time for one frame = " + elapsedTime + " ms");

            PatternCoordinates pc = new PatternCoordinates(new Point(),new Point(),new Point(),new Point(),0.0);
            if(out_send.getNum(2) !=  null) {
                pc = new PatternCoordinates(out_send.getNum(1), out_send.getNum(2), out_send.getNum(3), out_send.getNum(4), finalangle);
            }
            detectedPattern = pc;
        }

        detectedPattern = PatternCoordinates.flip(detectedPattern);

        return new Tuple<>(detectedPattern, rgba);
    }

    private double calculateExtraAngle(double x1, double y1, double x2, double y2){
        //Calculate the angle.
        double k = (y2 - y1) / (x1 - x2);
        double extra_angle=0;

        if ((k < -1) | (k > 1)) {
            if (y2 >= y1) {
                extra_angle = 180;
            } else {
                extra_angle = 0;
            }
        } else if ((-1 < k) | (k < 1)) {
            if (x1 >= x2) {
                extra_angle = 270;
            } else {
                extra_angle = 90;
            }
        }
        return extra_angle;
    }

    /**
     * Put the points in the right order so that they conform to the convention.
     * This method will rotate the given list of points until it finds the order where the first point is closest to the inner white square.
     *
     * @param point List of points representing the points of the pattern.
     * @param in_center Center of the small inner white square.
     * @return The points of the pattern in the right order.
     *          The angle is this patternCoordinator object is to be interpreted
     *          as the distance between Point 1 and the white square.
     */
    private PatternCoordinates Cal_Pointnum(Point[] point, Point in_center){
        Point[] point_send = new Point[4];
        double distance = 0; //Distance from Point to Inner Center.
        double min_dis= Double.POSITIVE_INFINITY; //Initialize at infinity.

        for (int i = 0;i<4;i++){
            distance = Math.sqrt(Math.pow(in_center.x - point[i].x,2)+Math.pow(in_center.y-point[i].y,2));
            if(distance<min_dis){
                //If the current point is closer to the white square then rearrange the points
                // so that the current point is the first point.
                min_dis = distance;

                int index = i;
                point_send[0] = point[index]; //Place the current point first.
                //Increase index each time to move clockwise through the points. Use modulo 4 so that after three comes zero. (4/4 = 1 -> Remainder = 0)
                point_send[1] = point[++index % 4];
                point_send[2] = point[++index % 4];
                point_send[3] = point[++index % 4];
                /*
                switch (i){
                    case 0:
                        point_send[0] = point[0];
                        point_send[1] = point[1];
                        point_send[2] = point[2];
                        point_send[3] = point[3];
                        break; //Break statements needed to prevent fallthrough.
                    case 1:
                        point_send[0] = point[1];
                        point_send[1] = point[2];
                        point_send[2] = point[3];
                        point_send[3] = point[0];
                        break;
                    case 2:
                        point_send[0] = point[2];
                        point_send[1] = point[3];
                        point_send[2] = point[0];
                        point_send[3] = point[1];
                        break;
                    case 3:
                        point_send[0] = point[3];
                        point_send[1] = point[0];
                        point_send[2] = point[1];
                        point_send[3] = point[2];
                        break;
                    }*/
            }
        }
        PatternCoordinates pc = new PatternCoordinates(point_send[0],point_send[1],point_send[2],point_send[3],distance);
        return pc;
    }

    /**
     * Filter out the contours in contour whose contourArea is smaller than dis*600 or larger than dis*10.
     *
     * @param size
     * @param contour List of contours
     * @return
     */
    private List<MatOfPoint> getContoursBySize(int size, List<MatOfPoint> contour) {
        List<MatOfPoint> con_in_range = new ArrayList<MatOfPoint>();
        Iterator<MatOfPoint> each = contour.iterator();
        while (each.hasNext()) {
            MatOfPoint contours = each.next();
            if ((Imgproc.contourArea(contours) < (size * 600)) & (Imgproc.contourArea(contours) > (100))) {
                con_in_range.add(contours);
            }
        }
        return con_in_range;
    }

    /**
     * Find contours which bestly resemble a square shape (sides of the bounding box are equal)
     *
     * @param con_in_range
     * @return
     */
    private List<MatOfPoint> getContoursSquare2(List<MatOfPoint> con_in_range) {
        List<MatOfPoint> squareContours = new ArrayList<MatOfPoint>();
        //List<Point> cl = new ArrayList<Point>();
        //Iterator<Point> con;
        Iterator<MatOfPoint> each_con = con_in_range.iterator();
        Rect out_rect = new Rect();

        while(each_con.hasNext()){
            MatOfPoint contours = each_con.next();
            out_rect = Imgproc.boundingRect(contours);

            if((Math.abs(out_rect.height - out_rect.width) < 10)){
                //&(3>(out_rect.area()/Imgproc.contourArea(contours)))
                //&(0.75<(out_rect.area()/Imgproc.contourArea(contours)))){
                squareContours.add(contours);
            }
        }
        return squareContours;
    }

    /**
     *
     * @param shapeContour
     * @return The center point of the shape.
     */
    private Point getShapeCenter(MatOfPoint shapeContour){
        Iterator<Point> con_point = shapeContour.toList().iterator();
        Point shapeCenter = new Point();
        double x_sum = 0;
        double y_sum = 0;
        int count = 0;
        while(con_point.hasNext()){
            //Add all x and y values.
            Point pt = con_point.next();
            x_sum += pt.x;
            y_sum += pt.y;
            count++;
        }
        shapeCenter.x = x_sum/count; //Take the average.
        shapeCenter.y = y_sum/count;
        return shapeCenter;
    }

    private List<MatOfPoint> findPattern(List<MatOfPoint> contours){
        double distance = Double.POSITIVE_INFINITY;
        List<MatOfPoint> patternContours = new ArrayList<MatOfPoint>();
        Iterator<MatOfPoint> iter1 = contours.iterator();
        while(iter1.hasNext()){
            MatOfPoint con1 = iter1.next();
            Point center1 = getShapeCenter(con1);
            Iterator<MatOfPoint> iter2 = contours.iterator();
            while(iter2.hasNext()){
                MatOfPoint con2 = iter2.next();
                Point center2 = getShapeCenter(con2);
                double dis_this = Math.abs(center1.x-center2.x)+Math.abs(center1.y-center2.y); //Distance between the the centers of inner and outer square.
                if((dis_this < distance) & (dis_this>0) ){//&(dis_this<70)){
                    distance = dis_this;
                    patternContours.clear();
                    patternContours.add(con1);
                    patternContours.add(con2);
                }
            }
        }
        return patternContours;
    }

    public int getDistance2(){
        return distance2;
    }
    public void setDistance(int dis){
        distance = dis;
    }
    public void setDistance2(int dis){
        distance2 = dis;
    }
    public void setSetupflag(boolean flag){
        setupflag = flag;
    }
}