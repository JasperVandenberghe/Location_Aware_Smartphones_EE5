package be.groept.emedialab.image_manipulation;

public class ImageSettings {

    public static final int BACKGROUND_MODE_RGB = 0;
    public static final int BACKGROUND_MODE_GRAYSCALE = 1;
    public static final int BACKGROUND_MODE_BINARY = 2;

    public static final int OVERLAY_PATTERN = 0;
    public static final int OVERLAY_SQUARE_BIG_CONTOURS = 1;
    public static final int OVERLAY_BIG_CONTOURS = 2;
    public static final int OVERLAY_CONTOURS = 3;

    private int backgroundMode = BACKGROUND_MODE_RGB;

    private boolean overlayPatternEnabled = false;
    private boolean overlaySquareBigContoursEnabled = false;
    private boolean overlayBigContoursEnabled = false;
    private boolean overlayContoursEnabled = false;

    public int getBackgroundMode(){
        return backgroundMode;
    }

    public void setBackgroundMode(int backgroundMode){
        this.backgroundMode = backgroundMode;
    }

    public boolean getOverlayEnabled(int type){
        switch(type){
            case OVERLAY_PATTERN:
                return overlayPatternEnabled;
            case OVERLAY_SQUARE_BIG_CONTOURS:
                return overlaySquareBigContoursEnabled;
            case OVERLAY_BIG_CONTOURS:
                return overlayBigContoursEnabled;
            case OVERLAY_CONTOURS:
                return overlayContoursEnabled;
        }
        return false;
    }

    public void setOverlayEnabled(int type, boolean value){
        switch(type){
            case OVERLAY_PATTERN:
                overlayPatternEnabled = value;
                break;
            case OVERLAY_SQUARE_BIG_CONTOURS:
                overlaySquareBigContoursEnabled = value;
                break;
            case OVERLAY_BIG_CONTOURS:
                overlayBigContoursEnabled = value;
                break;
            case OVERLAY_CONTOURS:
                overlayContoursEnabled = value;
                break;
        }
    }

}
