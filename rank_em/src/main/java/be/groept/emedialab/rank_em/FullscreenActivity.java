package be.groept.emedialab.rank_em;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mContentView = findViewById(R.id.fullscreen_content);
        Button createButton = (Button) findViewById(R.id.createGame);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SecondActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("create", true);
                intent.putExtras(bundle);
                FullscreenActivity.this.startActivity(intent);
            }
        });

        Button joinButton = (Button) findViewById(R.id.joinGame);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SecondActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("create", false);
                intent.putExtras(bundle);
                FullscreenActivity.this.startActivity(intent);
            }
        });

        Log.d("Dp", "Height " + getResources().getConfiguration().screenHeightDp);
        Log.d("Dp", "Width " + getResources().getConfiguration().screenWidthDp);
        //Log.d("Dp", "DPI " + getResources().getConfiguration().densityDpi);

        hide();
    }

    @SuppressLint("InlinedApi")
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Lollipop and higher
        if(Build.VERSION.SDK_INT >= 21){
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hide();
    }
}
