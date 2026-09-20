package com.example.softbackgrounds.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class MotionProvider implements SensorEventListener {
    protected final Context context;
    protected final SensorManager sensorManager;
    protected Sensor sensor;
    protected final RawMotionState rawMotionState;
    protected RawMotionStateListener motionListener;

    public MotionProvider(Context context) {
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rawMotionState = new RawMotionState();
    }

    public void setMotionListener(RawMotionStateListener listener) {
        this.motionListener = listener;
    }

    public abstract void start();

    public abstract void stop();

    @Override
    public abstract void onSensorChanged(SensorEvent event);

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
