package com.example.softbackgrounds.motion.providers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.softbackgrounds.motion.MotionProvider;

public class RotationVectorProvider extends MotionProvider {

    private static final int YAW = 0;
    private static final int PITCH = 1;
    private static final int ROLL = 2;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    public RotationVectorProvider(Context context) {
        super(context);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    public void start() {
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR)
            return;

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);

        rawMotionState.yaw = (float) Math.toDegrees(orientation[YAW]);
        rawMotionState.pitch = (float) Math.toDegrees(orientation[PITCH]);
        rawMotionState.roll = (float) Math.toDegrees(orientation[ROLL]);

        if (motionListener != null) {
            motionListener.onMotionChanged(rawMotionState);
        }
    }
}
