package com.example.softbackgrounds.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class MotionEngine implements RawMotionStateListener {

    private final MotionProvider motionProvider;
    private final SensorManager sensorManager;
    private final MotionState motionState;

    private MotionStateListener motionStateListener;
    private MotionProviderType providerType;

    // --------------------------------------------------
    // Calibration
    // --------------------------------------------------

    private static final int CALIBRATION_SAMPLES = 50;

    private int calibrationSampleCount = 0;

    private float calibrationRoll = 0.0f;
    private float calibrationPitch = 0.0f;
    private float calibrationYaw = 0.0f;

    private boolean calibrated = false;


    // --------------------------------------------------
    // Filtering
    // --------------------------------------------------

    /*
     * 0.0 = extremely smooth / slow
     * 1.0 = no filtering
     *
     * Around 0.10 - 0.20 works nicely for wallpapers.
     */
    private static final float FILTER_STRENGTH = 0.12f;

    private float filteredRoll = 0.0f;
    private float filteredPitch = 0.0f;
    private float filteredYaw = 0.0f;

    private boolean filterInitialized = false;


    // --------------------------------------------------
    // Normalization
    // --------------------------------------------------

    /*
     * Tilting the phone by approximately this amount
     * will produce normalized value +/- 1.
     */
    private static final float MAX_TILT = 45.0f;

    /*
     * Small movements around the center are ignored.
     * This prevents the wallpaper from constantly moving
     * because of tiny sensor fluctuations.
     */
    private static final float DEAD_ZONE = 0.03f;


    public MotionEngine(Context context) {

        sensorManager =
                (SensorManager) context.getSystemService(
                        Context.SENSOR_SERVICE
                );

        detectProvider();

        motionProvider =
                MotionProviderFactory.createProvider(
                        context,
                        providerType
                );

        motionState = new MotionState();

        motionState.motionSupported =
                providerType != MotionProviderType.NONE;

        if (motionProvider != null) {
            motionProvider.setMotionListener(this);
        }
    }


    // ==================================================
    // Start / Stop
    // ==================================================

    public void start() {

        resetCalibration();

        if (motionProvider != null) {
            motionProvider.start();
        }
    }


    public void stop() {

        if (motionProvider != null) {
            motionProvider.stop();
        }
    }


    // ==================================================
    // Listener
    // ==================================================

    public void setMotionStateListener(
            MotionStateListener listener) {

        this.motionStateListener = listener;
    }


    // ==================================================
    // Raw Motion Callback
    // ==================================================

    @Override
    public void onMotionChanged(
            RawMotionState rawMotionState) {

        if (rawMotionState == null) {
            return;
        }

        process(rawMotionState);
    }


    // ==================================================
    // Motion Processing Pipeline
    // ==================================================

    private void process(
            RawMotionState rawMotionState) {

        float rawRoll = rawMotionState.roll;
        float rawPitch = rawMotionState.pitch;
        float rawYaw = rawMotionState.yaw;


        // --------------------------------------------------
        // 1. Calibration
        // --------------------------------------------------

        if (!calibrated) {

            calibrationRoll += rawRoll;
            calibrationPitch += rawPitch;
            calibrationYaw += rawYaw;

            calibrationSampleCount++;

            if (calibrationSampleCount >= CALIBRATION_SAMPLES) {

                calibrationRoll /=
                        CALIBRATION_SAMPLES;

                calibrationPitch /=
                        CALIBRATION_SAMPLES;

                calibrationYaw /=
                        CALIBRATION_SAMPLES;

                calibrated = true;

                filterInitialized = false;
            }

            // Don't send unstable calibration values
            // to the renderer.
            motionState.roll = 0.0f;
            motionState.pitch = 0.0f;
            motionState.yaw = 0.0f;

            motionState.normalizedRoll = 0.0f;
            motionState.normalizedPitch = 0.0f;

            motionState.motionSupported =
                    providerType != MotionProviderType.NONE;

            notifyListener();

            return;
        }


        // --------------------------------------------------
        // 2. Remove calibration offset
        // --------------------------------------------------

        float calibratedRoll =
                normalizeAngle(
                        rawRoll - calibrationRoll
                );

        float calibratedPitch =
                normalizeAngle(
                        rawPitch - calibrationPitch
                );

        float calibratedYaw =
                normalizeAngle(
                        rawYaw - calibrationYaw
                );


        // --------------------------------------------------
        // 3. Low-pass filtering
        // --------------------------------------------------

        if (!filterInitialized) {

            filteredRoll = calibratedRoll;
            filteredPitch = calibratedPitch;
            filteredYaw = calibratedYaw;

            filterInitialized = true;

        } else {

            filteredRoll = lowPass(
                    filteredRoll,
                    calibratedRoll
            );

            filteredPitch = lowPass(
                    filteredPitch,
                    calibratedPitch
            );

            filteredYaw = lowPass(
                    filteredYaw,
                    calibratedYaw
            );
        }


        // --------------------------------------------------
        // 4. Store filtered values
        // --------------------------------------------------

        motionState.roll = filteredRoll;
        motionState.pitch = filteredPitch;
        motionState.yaw = filteredYaw;


        // --------------------------------------------------
        // 5. Normalize
        // --------------------------------------------------

        float normalizedRoll =
                filteredRoll / MAX_TILT;

        float normalizedPitch =
                filteredPitch / MAX_TILT;


        // --------------------------------------------------
        // 6. Clamp to -1 ... +1
        // --------------------------------------------------

        normalizedRoll =
                clamp(
                        normalizedRoll,
                        -1.0f,
                        1.0f
                );

        normalizedPitch =
                clamp(
                        normalizedPitch,
                        -1.0f,
                        1.0f
                );


        // --------------------------------------------------
        // 7. Dead zone
        // --------------------------------------------------

        if (Math.abs(normalizedRoll) < DEAD_ZONE) {
            normalizedRoll = 0.0f;
        }

        if (Math.abs(normalizedPitch) < DEAD_ZONE) {
            normalizedPitch = 0.0f;
        }


        // --------------------------------------------------
        // 8. Store normalized values
        // --------------------------------------------------

        motionState.normalizedRoll =
                normalizedRoll;

        motionState.normalizedPitch =
                normalizedPitch;

        motionState.motionSupported =
                providerType != MotionProviderType.NONE;


        // --------------------------------------------------
        // 9. Send processed state
        // --------------------------------------------------

        notifyListener();
    }


    // ==================================================
    // Low Pass Filter
    // ==================================================

    private float lowPass(
            float current,
            float target) {

        return current +
                (target - current)
                        * FILTER_STRENGTH;
    }


    // ==================================================
    // Angle Normalization
    // ==================================================

    private float normalizeAngle(float angle) {

        while (angle > 180.0f) {
            angle -= 360.0f;
        }

        while (angle < -180.0f) {
            angle += 360.0f;
        }

        return angle;
    }


    // ==================================================
    // Clamp
    // ==================================================

    private float clamp(
            float value,
            float min,
            float max) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }


    // ==================================================
    // Notify Listener
    // ==================================================

    private void notifyListener() {

        if (motionStateListener != null) {

            motionStateListener.onMotionStateChanged(
                    motionState
            );
        }
    }


    // ==================================================
    // Reset Calibration
    // ==================================================

    public void resetCalibration() {

        calibrationSampleCount = 0;

        calibrationRoll = 0.0f;
        calibrationPitch = 0.0f;
        calibrationYaw = 0.0f;

        filteredRoll = 0.0f;
        filteredPitch = 0.0f;
        filteredYaw = 0.0f;

        calibrated = false;
        filterInitialized = false;

        motionState.roll = 0.0f;
        motionState.pitch = 0.0f;
        motionState.yaw = 0.0f;

        motionState.normalizedRoll = 0.0f;
        motionState.normalizedPitch = 0.0f;
    }


    // ==================================================
    // Provider Detection
    // ==================================================

    private void detectProvider() {

        if (sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR
        ) != null) {

            providerType =
                    MotionProviderType.ROTATION_VECTOR;

        } else if (sensorManager.getDefaultSensor(
                Sensor.TYPE_GAME_ROTATION_VECTOR
        ) != null) {

            providerType =
                    MotionProviderType.GAME_ROTATION_VECTOR;

        } else if (
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER
                ) != null
                        &&
                        sensorManager.getDefaultSensor(
                                Sensor.TYPE_MAGNETIC_FIELD
                        ) != null
        ) {

            providerType =
                    MotionProviderType.ACCELEROMETER_MAGNETOMETER;

        } else if (
                sensorManager.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER
                ) != null
        ) {

            providerType =
                    MotionProviderType.ACCELEROMETER;

        } else {

            providerType =
                    MotionProviderType.NONE;
        }
    }


    public MotionProviderType getProviderType() {

        return providerType;
    }
}