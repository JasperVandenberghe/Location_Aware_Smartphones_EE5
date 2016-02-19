package be.groept.emedialab.animations.confetti;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import be.groept.emedialab.R;

public class ConfettiFallView extends View{

    private int confettiCount = 10;
    private final List<Drawable> drawables = new ArrayList<>();
    private int[][] coords;
    private final Drawable confettiFlakeRed;
    private final Drawable confettiFlakeGreen;
    private final Drawable confettiFlakeBlue;
    private final Drawable confettiFlakeOrange;
    private final Drawable confettiRibbonPink;
    private final Drawable confettiRibbonYellow;
    private final Drawable balloon;

    public ConfettiFallView(Context context){
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        confettiFlakeRed = context.getResources().getDrawable(R.drawable.confetti_flake_red);
        confettiFlakeGreen = context.getResources().getDrawable(R.drawable.confetti_flake_green);
        confettiFlakeBlue = context.getResources().getDrawable(R.drawable.confetti_flake_blue);
        confettiFlakeOrange = context.getResources().getDrawable(R.drawable.confetti_flake_orange);
        confettiRibbonPink = context.getResources().getDrawable(R.drawable.confetti_ribbon_pink);
        confettiRibbonYellow = context.getResources().getDrawable(R.drawable.confetti_ribbon_yellow);
        balloon = context.getResources().getDrawable(R.drawable.balloon);

        assert confettiFlakeRed != null;
        confettiFlakeRed.setBounds(0, 0, confettiFlakeRed.getIntrinsicWidth(), confettiFlakeRed.getIntrinsicHeight());
        assert confettiFlakeGreen != null;
        confettiFlakeGreen.setBounds(0, 0, confettiFlakeGreen.getIntrinsicWidth(), confettiFlakeGreen.getIntrinsicHeight());
        assert confettiFlakeBlue !=  null;
        confettiFlakeBlue.setBounds(0, 0, confettiFlakeBlue.getIntrinsicWidth(), confettiFlakeBlue.getIntrinsicHeight());
        assert confettiFlakeOrange != null;
        confettiFlakeOrange.setBounds(0, 0, confettiFlakeOrange.getIntrinsicWidth(), confettiFlakeOrange.getIntrinsicHeight());
        assert confettiRibbonPink != null;
        confettiRibbonPink.setBounds(0, 0, confettiRibbonPink.getIntrinsicWidth(), confettiRibbonPink.getIntrinsicHeight());
        assert confettiRibbonYellow != null;
        confettiRibbonYellow.setBounds(0, 0, confettiRibbonYellow.getIntrinsicWidth(), confettiRibbonYellow.getIntrinsicHeight());
        assert balloon != null;
        balloon.setBounds(0, 0, balloon.getIntrinsicWidth(), balloon.getIntrinsicHeight());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        Random random = new Random();
        Interpolator interpolator = new OvershootInterpolator();

        confettiCount = Math.max(width, height)/20;
        coords = new int[confettiCount][];
        drawables.clear();
        for(int i = 0; i < confettiCount; i++){
            Animation animation = new TranslateAnimation(0, height/10 - random.nextInt(height/5), -200, height + 30);
            animation.setDuration(20 * height + random.nextInt(5 * height));
            animation.setRepeatCount(-1);
            animation.initialize(10, 10, 10, 10);
            animation.setInterpolator(interpolator);

            Animation balloonAnimation = new TranslateAnimation(height/10 - random.nextInt(height/5), 0, height + 30, -200);
            balloonAnimation.setDuration(20 * height + random.nextInt(5 * height));
            balloonAnimation.setRepeatCount(-1);
            balloonAnimation.initialize(10, 10, 10, 10);
            balloonAnimation.setInterpolator(interpolator);

            coords[i] = new int[]{random.nextInt(width - 30), -30};
            int balloonConfettiRatio = random.nextInt(20);
            if( balloonConfettiRatio > 13)
                drawables.add(new AnimateDrawable(balloon, balloonAnimation));
            else{
                int randomNumber = random.nextInt(10);
                int randomConfetti = random.nextInt(10);
                if(randomNumber < 7) {
                    if(randomNumber % 2 == 0){
                        if(randomConfetti % 2 == 0)
                            drawables.add(new AnimateDrawable(confettiFlakeRed, animation));
                        else
                            drawables.add(new AnimateDrawable(confettiFlakeGreen, animation));
                    }
                    else{
                        if(randomConfetti % 2 == 0)
                            drawables.add(new AnimateDrawable(confettiFlakeBlue, animation));
                        else
                            drawables.add(new AnimateDrawable(confettiFlakeOrange, animation));
                    }
                }
                else{
                    if(randomConfetti % 2 == 0)
                        drawables.add(new AnimateDrawable(confettiRibbonPink, animation));
                    else
                        drawables.add(new AnimateDrawable(confettiRibbonYellow, animation));
                }
            }

            animation.setStartOffset(random.nextInt(20 * height));
            animation.startNow();
            balloonAnimation.setStartOffset(random.nextInt(20 * height));
            balloonAnimation.startNow();

        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        for(int i = 0; i < confettiCount; i++){
            Drawable drawable = drawables.get(i);
            canvas.save();
            canvas.translate(coords[i][0], coords[i][1]);
            drawable.draw(canvas);
            canvas.restore();
        }
        invalidate();
    }


}
