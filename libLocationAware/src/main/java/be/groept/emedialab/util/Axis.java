package be.groept.emedialab.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class Axis extends ImageView{
    Paint paint = new Paint();

    public Axis(Context context) {
        super(context);
    }

    public Axis(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        /*paint.setColor(Color.RED);
        paint.setStrokeWidth(4);

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);

        int scale = 50;
        int centreX = this.getWidth()/2;
        int centreY = this.getZ()/2;
        canvas.drawLine(centreX, centreY,
                (float) (centreX +  xVector.getX()*scale),
                (float) (centreY - xVector.getY()*scale),
                paint);
        canvas.drawLine(centreX, this.getZ()/2,
                (float) (centreX + yVector.getX()*scale),
                (float) (centreY - yVector.getY()*scale)
                , paint);*/
        super.onDraw(canvas);
    }
}
