package com.example.softbackgrounds.motion.providers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.softbackgrounds.motion.MotionProvider;

public class AccelerometerProvider extends MotionProvider {

    private Sensor accelerometer;
    private final float[] acceleration = new float[3];

    public AccelerometerProvider(Context context) {
        super(context);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() !=
                Sensor.TYPE_ACCELEROMETER)
            return;

        System.arraycopy(event.values, 0, acceleration, 0, 3);
        calculateOrientation();

        Log.d("ACC", "X = " + event.values[0] + " Y = " + event.values[1] + " Z = " + event.values[2]);

        if (motionListener != null) {
            motionListener.onMotionChanged(rawMotionState);
        }
    }

    private void calculateOrientation() {
        float x = acceleration[0];
        float y = acceleration[1];
        float z = acceleration[2];

        //rawMotionState.roll = (float) Math.toDegrees(Math.atan2(y, z));

        rawMotionState.roll = (float) Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));
        rawMotionState.pitch = (float) Math.toDegrees(Math.atan2(-x, Math.sqrt(y * y + z * z)));

        //rawMotionState.pitch = (float) Math.toDegrees(Math.atan2(-x, Math.sqrt(y * y + z * z)));

        rawMotionState.yaw = 0f;
    }
}