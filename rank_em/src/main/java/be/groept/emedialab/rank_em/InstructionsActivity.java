package be.groept.emedialab.rank_em;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.util.GlobalResources;

/**
 * An activity to display the instructions of the game
 */
public class InstructionsActivity extends AppCompatActivity{

    final Activity activity = this;
    private View mContentView;

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_START_GAME){
                startActivity(new Intent(activity, GameActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalResources.getInstance().setHandler(handler);

        setContentView(R.layout.activity_instructions);
        mContentView = findViewById(R.id.fullscreen_content);

        TextView instructions = (TextView) findViewById(R.id.instructions);
        instructions.setText("These are the instructions");

        Button gotIt = (Button) findViewById(R.id.buttonGotIt);

        if(GlobalResources.getInstance().getClient()){
            gotIt.setVisibility(View.GONE);
        } else {
            gotIt.setVisibility(View.VISIBLE);
            gotIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GlobalResources.getInstance().sendData(null, DataHandler.DATA_TYPE_START_GAME, null);
                    startActivity(new Intent(activity, GameActivity.class));
                }
            });
        }

        hide();
    }

    @SuppressLint("InlinedApi")
    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if(Build.VERSION.SDK_INT >= 21) {
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
