package be.groept.emedialab.animal_farm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.util.GlobalResources;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class InstructionsActivity extends AppCompatActivity {

    final Activity activity = this;
    private View mContentView;
    private MediaPlayer player = new MediaPlayer();

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_START_GAME)
                startActivity(new Intent(activity, GameActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        GlobalResources.getInstance().setHandler(handler);

        setContentView(R.layout.activity_instructions);
        mContentView = findViewById(R.id.fullscreen_content);

        final ImageButton doneButton = (ImageButton) findViewById(R.id.doneButton);
        final ImageButton refreshButton  = (ImageButton) findViewById(R.id.refreshButton);
        if(!GlobalResources.getInstance().getClient()){
            doneButton.setVisibility(View.VISIBLE);
            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GlobalResources.getInstance().sendData(null, DataHandler.DATA_TYPE_START_GAME, null);
                    startActivity(new Intent(activity, GameActivity.class));
                }
            });
            VideoView video = (VideoView) findViewById(R.id.video);
            video.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.instructions_video);
            video.setVideoURI(videoUri);
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });
            video.start();
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.boerderij_instructies);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    /*doneButton.setVisibility(View.VISIBLE);
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GlobalResources.getInstance().sendData(null, DataHandler.DATA_TYPE_START_GAME, null);
                            startActivity(new Intent(activity, GameActivity.class));
                        }
                    });*/
                    refreshButton.setVisibility(View.VISIBLE);
                    refreshButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaPlayer.start();
                        }
                    });
                }
            });
            player = mediaPlayer;
        }

        hide();
    }

    @SuppressLint("InlinedApi")
    private void hide(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();

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
    protected void onResume(){
        super.onResume();
        hide();
    }

    @Override
    protected void onPause(){
        super.onPause();
        player.stop();
    }
}
