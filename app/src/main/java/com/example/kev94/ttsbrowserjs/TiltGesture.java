package com.example.kev94.ttsbrowserjs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Kev94 on 24.09.2015.
 */
public class TiltGesture implements iGestures, SensorEventListener {

    private static final double N2S = 1.0 / 1000000000.0;

    //constants for the shake gesture
    public static final double TILT_THRESHOLD = Math.PI/12;
    public static final long TILT_MAX_PAUSE_TIME = 1000000000;

    private SensorManager mSensorManager;

    private Sensor mGyro;

    private boolean mFirstCall = true;

    private long mPreviousTimestamp = 0;

    private double mCurrentAngle = 0;

    private boolean mPause = false;

    public TiltGesture(Context context) {

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        } else {
            Toast.makeText(context, context.getString(R.string.toast_no_gyro), Toast.LENGTH_LONG).show();
        }

        registerListener();
    }

    @Override
    public void registerListener() {
        if(mGyro != null) {
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i("Gesture", "Accelerometer registered");
        }
    }

    @Override
    public void unregisterListener() {
        if(mGyro != null) {
            Log.i("Gesture", "unregister Accelerometer");
            mSensorManager.unregisterListener(this, mGyro);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(!mFirstCall) {
            long dT = event.timestamp - mPreviousTimestamp;

            mCurrentAngle += event.values[0] * dT * N2S;
        }
        else {
            mFirstCall = false;
        }

        if(mPause && (Math.abs(mCurrentAngle) < (Math.PI/100))) {
            mCurrentAngle = 0;
            mPause = false;
        }

        mPreviousTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int gestureCompleted() {

        if(!mPause && (mCurrentAngle > TILT_THRESHOLD)) {
            Log.i("Seonsor", "positive");
            mPause = true;
            return 1;
        } else if(!mPause && (mCurrentAngle < (-TILT_THRESHOLD))) {
            Log.i("Seonsor", "negative");
            mPause = true;
            return -1;
        }
        return 0;
    }
}
