package be.groept.emedialab.movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import be.groept.emedialab.util.GlobalResources;

/*
Implementation of MovementInterface based on reading out the raw values of the accelerometer and filtering them.
Based on http://stackoverflow.com/a/14574992/1319187
 */
public class MovementAccelerometer implements SensorEventListener {

    private static final String TAG = "MovementAccelerometer";

    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean isMoving = false;

    public MovementAccelerometer(Context mContext) {
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] g = event.values.clone();

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

            float delta = currentAcceleration - lastAcceleration;
            acceleration = Math.abs(acceleration * 0.9f + delta);
            //Log.d(TAG, "Current acceleration [" + Math.round(currentAcceleration) + "] last acceleration [" + Math.round(lastAcceleration) + "] delta[" + Math.round(delta) + "], acceleration[" + acceleration + "]");

            //isMoving changed
            if (acceleration > 0.5 != isMoving) {
                isMoving = !isMoving;
                GlobalResources.getInstance().setMoving(isMoving);
            }

            calculateTilt(g);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG, "Accuracy of accelerometer changed to " + accuracy);
        }
    }

    private void calculateTilt(float[] g){
        if(g[0] < -1 && g[0] > 1 && g[1] < -1 && g[1] > 1 && g[2] > 8.5 && g[2] < 10.5){
            GlobalResources.getInstance().setTilted(true);
        }else{
            GlobalResources.getInstance().setTilted(false);
        }
    }
}
