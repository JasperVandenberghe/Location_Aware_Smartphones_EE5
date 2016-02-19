package be.groept.emedialab.rank_em;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import be.groept.emedialab.animations.confetti.BoardFallView;
import be.groept.emedialab.animations.confetti.ConfettiFallView;
import be.groept.emedialab.util.GlobalResources;

public class WinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);
        ConfettiFallView balloonFall = new ConfettiFallView(this);
        BoardFallView boardFallView = new BoardFallView(this);
        FrameLayout insertPoint = (FrameLayout) findViewById(R.id.balloonView);
        insertPoint.addView(balloonFall);
        FrameLayout insert = (FrameLayout) findViewById(R.id.boardView);
        insert.addView(boardFallView);

        View mContentView = findViewById(R.id.fullscreen_content);
        hide();
    }

    @SuppressLint("InlinedApi")
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
    }
}
