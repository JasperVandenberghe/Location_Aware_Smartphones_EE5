package be.groept.emedialab.arrow;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import be.groept.emedialab.communications.DataHandler;
import be.groept.emedialab.communications.DataPacket;
import be.groept.emedialab.image_manipulation.RunPatternDetector;
import be.groept.emedialab.server.data.Position;
import be.groept.emedialab.util.GlobalResources;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ArrowGame extends AppCompatActivity {

    private static final String TAG = "ArrowGame";
    public static final int TYPE_CO = 0;
    public static final int TYPE_TIMESTAMP_FROM_CLIENT = 1;
    public static final int TYPE_TIMESTAMP_FROM_SERVER = 2;

    private View mContentView;
    private ImageView mImageView;
    private TextView bluetoothDelayTextView;
    private TextView ownPositionTextView;
    private TextView otherPositionTextView;
    private Position otherPosition = null;

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == DataHandler.DATA_TYPE_DATA_PACKET){
                Serializable data;
                if((data = GlobalResources.getInstance().readData()) != null)
                    handleData((DataPacket) data);
            }else if(msg.what == DataHandler.DATA_TYPE_COORDINATES){
                otherPosition = (Position) msg.obj;
                updateRotation();
                updatePosition(otherPosition, otherPositionTextView, "Other");
            }else if(msg.what == DataHandler.DATA_TYPE_DEVICE_DISCONNECTED){
                BluetoothDevice device = (BluetoothDevice) msg.obj;
                Toast.makeText(ArrowGame.this, "Device " + device.getAddress() + " disconnected!", Toast.LENGTH_SHORT).show();
            }else if(msg.what == DataHandler.DATA_TYPE_OWN_POS_UPDATED){
                if(!GlobalResources.getInstance().getClient()){
                    //The server his position is updated, send this to the client!
                    GlobalResources.getInstance().sendData(new DataPacket(TYPE_CO, (Position) msg.obj));
                }
                updateRotation();
                updatePosition((Position) msg.obj, ownPositionTextView, "Self");
            }
        }
    };

    protected void hey(){
        int i = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_game);

        mContentView = findViewById(R.id.relativeLayout);

        mImageView = (ImageView) findViewById(R.id.arrow);
        mImageView.setRotation(50);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GlobalResources.getInstance().getClient()){
                    GlobalResources.getInstance().sendData(new DataPacket(TYPE_TIMESTAMP_FROM_CLIENT, System.currentTimeMillis()));
                }else{
                    GlobalResources.getInstance().sendData(new DataPacket(TYPE_TIMESTAMP_FROM_SERVER, System.currentTimeMillis()));
                }
            }
        });

        bluetoothDelayTextView = (TextView) findViewById(R.id.bluetoothDelay);

        ownPositionTextView = (TextView) findViewById(R.id.ownPosition);

        otherPositionTextView = (TextView) findViewById(R.id.otherPosition);

        GlobalResources.getInstance().setHandler(handler);

        final Activity activity = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new RunPatternDetector(activity);
            }
        };
        runnable.run();

        hide();
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if(Build.VERSION.SDK_INT >= 21){
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void updateRotation(){
        Position currPosition = GlobalResources.getInstance().getDevice().getPosition();
        if(otherPosition != null && otherPosition.getFoundPattern() && currPosition != null && currPosition.getFoundPattern()){
            float angle = (float) Math.toDegrees(Math.atan2(currPosition.getY() - otherPosition.getY(), currPosition.getX() - otherPosition.getX()));
            if (angle < 0)
                angle += 360;

            Log.d(TAG, "Angle: " + angle);
            mImageView.setRotation(angle + 180 + (float) currPosition.getRotation());
        }
    }

    private void updatePosition(Position position, TextView textView, String prefix){
        if(position != null){
            if(position.getFoundPattern()){
                textView.setTextColor(Color.parseColor("green"));
                textView.setText(String.format("%s: (%.2f, %.2f, %.2f) %.1f°", prefix, position.getX(), position.getY(), position.getZ(), position.getRotation()));
            }else{
                textView.setTextColor(Color.parseColor("red"));
            }
        }
    }

    private void updateBluetoothDelay(long milliseconds){
        bluetoothDelayTextView.setText(String.format("BT delay: %d ms", milliseconds));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GlobalResources.getInstance().getPatternDetector() != null && GlobalResources.getInstance().getPatternDetector().isPaused())
            GlobalResources.getInstance().getPatternDetector().setup();
        hide();
    }

    @Override
    protected void onPause(){
        super.onPause();
        GlobalResources.getInstance().getPatternDetector().destroy();
    }

    private void handleData(DataPacket dataPacket){
        switch (dataPacket.getDataType()){
            case TYPE_CO: // only run by client
                otherPosition = (Position) dataPacket.getOptionalData();
                updateRotation();
                Log.d(TAG, "RECEIVED COORDINATES FROM SERVER: " + otherPosition);
                updatePosition(otherPosition, otherPositionTextView, "Other");
                break;
            case TYPE_TIMESTAMP_FROM_CLIENT:
                long timestamp = (long) dataPacket.getOptionalData();
                if(GlobalResources.getInstance().getClient()){
                    updateBluetoothDelay((System.currentTimeMillis() - timestamp));
                }else{
                    GlobalResources.getInstance().sendData(new DataPacket(TYPE_TIMESTAMP_FROM_CLIENT, timestamp));
                }
                break;
            case TYPE_TIMESTAMP_FROM_SERVER:
                long ts = (long) dataPacket.getOptionalData();
                if(!GlobalResources.getInstance().getClient()){
                    updateBluetoothDelay((System.currentTimeMillis() - ts));
                }else{
                    GlobalResources.getInstance().sendData(new DataPacket(TYPE_TIMESTAMP_FROM_SERVER, ts));
                }
                break;
        }
    }
}
