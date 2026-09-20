package com.example.softbackgrounds.motion.providers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.example.softbackgrounds.motion.MotionProvider;

public class AccelerometerMagnetometerProvider
        extends MotionProvider {

    private Sensor accelerometer;

    private Sensor magnetometer;

    private final float[] gravity = new float[3]; //accelerometer values

    private final float[] geomagnetic = new float[3]; //magnetometer values

    private final float[] rotationMatrix = new float[9];

    private final float[] orientation = new float[3];

    private static final int YAW = 0;

    private static final int PITCH = 1;

    private static final int ROLL = 2;

    public AccelerometerMagnetometerProvider(
            Context context) {

        super(context);

        accelerometer =
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER);

        magnetometer =
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_MAGNETIC_FIELD);

    }

    @Override
    public void start() {

        if (accelerometer != null) {

            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);

        }

        if (magnetometer != null) {

            sensorManager.registerListener(
                    this,
                    magnetometer,
                    SensorManager.SENSOR_DELAY_GAME);

        }

    }

    @Override
    public void stop() {

        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(
            SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:

                System.arraycopy(
                        event.values,
                        0,
                        gravity,
                        0,
                        3);

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:

                System.arraycopy(
                        event.values,
                        0,
                        geomagnetic,
                        0,
                        3);

                break;

        }

        boolean success =
                SensorManager.getRotationMatrix(
                        rotationMatrix,
                        null,
                        gravity,
                        geomagnetic);

        if (!success)
            return;

        SensorManager.getOrientation(
                rotationMatrix,
                orientation);

        rawMotionState.yaw =
                (float) Math.toDegrees(
                        orientation[YAW]);

        rawMotionState.pitch =
                (float) Math.toDegrees(
                        orientation[PITCH]);

        rawMotionState.roll =
                (float) Math.toDegrees(
                        orientation[ROLL]);

        if (motionListener != null) {

            motionListener.onMotionChanged(
                    rawMotionState);

        }

    }

}