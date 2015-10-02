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
public class NodGesture implements iGestures, SensorEventListener {

    //constants for the shake gesture
    public static final double NOD_THRESHOLD = 3;
    public static final long NOD_MAX_EVENT_TIME = 200000000;
    public static final long NOD_MAX_TIME_BETWEEN_EVENTS = 1000000000;

    private SensorManager mSensorManager;

    private Sensor mLinAccel;

    private boolean mGestureCompleted = false;

    private long mCurrentTimestamp = 0;
    private long mPreviousTimestamp = 0;

    private int mNodCount = 0;

    public NodGesture(Context context) {

        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mLinAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        } else {
            Toast.makeText(context, context.getString(R.string.toast_no_accel), Toast.LENGTH_LONG).show();
        }

        registerListener();
    }

    @Override
    public void registerListener() {
        if(mLinAccel != null) {
            mSensorManager.registerListener(this, mLinAccel, SensorManager.SENSOR_DELAY_FASTEST);
            Log.i("Gesture", "Nod Accelerometer registered");
        }
    }

    @Override
    public void unregisterListener() {
        if(mLinAccel != null) {
            Log.i("Gesture", "unregister Nod Accelerometer");
            mSensorManager.unregisterListener(this, mLinAccel);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(!mGestureCompleted) {
            if ((event.timestamp - mPreviousTimestamp) > NOD_MAX_TIME_BETWEEN_EVENTS) {
                mNodCount = 0;
                mCurrentTimestamp = 0;
                mPreviousTimestamp = 0;
            }

            if (Math.abs(event.values[2]) > NOD_THRESHOLD) {
                if ((event.timestamp - mCurrentTimestamp) > NOD_MAX_EVENT_TIME) {
                    Log.i("Gesture", "Value: " + Double.toString(event.values[0]) + " nods: " + Integer.toString(mNodCount) + " time: " + Long.toString(event.timestamp - mCurrentTimestamp));
                    mCurrentTimestamp = event.timestamp;
                    mNodCount++;
                    mPreviousTimestamp = mCurrentTimestamp;
                }
            }

            if (mNodCount > 2) {
                mGestureCompleted = true;
                unregisterListener();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int gestureCompleted() {

        if(mGestureCompleted) {
            return 1;
        }

        return 0;
    }
}
