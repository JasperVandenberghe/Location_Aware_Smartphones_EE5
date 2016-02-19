package be.groept.emedialab.animations.confetti;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.List;

import be.groept.emedialab.R;

public class BoardFallView extends View{

    private final List<Drawable> drawables = new ArrayList<>();
    private final Drawable board;

    public BoardFallView(Context context){
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        board = context.getResources().getDrawable(R.drawable.hanging_board);
        assert board != null;
        board.setBounds(0, 0, board.getIntrinsicWidth(), board.getIntrinsicHeight());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        drawables.clear();
        Animation dropBoardAnimation = new TranslateAnimation(width/2 - board.getIntrinsicWidth()/2, width/2 - board.getIntrinsicWidth()/2, -200, height/2 - board.getIntrinsicHeight());
        dropBoardAnimation.setDuration(3000);
        dropBoardAnimation.initialize(10, 10, 10, 10);
        Interpolator interpolator = new LinearInterpolator();
        dropBoardAnimation.setInterpolator(interpolator);
        drawables.add(new AnimateDrawable(board, dropBoardAnimation));
        dropBoardAnimation.startNow();
    }

    @Override
    protected void onDraw(Canvas canvas){
        for(int i = 0; i < drawables.size(); i++){
            Drawable drawable = drawables.get(i);
            canvas.save();
            drawable.draw(canvas);
            canvas.restore();
        }
        invalidate();
    }
}
