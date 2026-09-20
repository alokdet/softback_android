package com.example.softbackgrounds.motion;


import android.content.Context;

import com.example.softbackgrounds.motion.providers.AccelerometerMagnetometerProvider;
import com.example.softbackgrounds.motion.providers.AccelerometerProvider;
import com.example.softbackgrounds.motion.providers.GameRotationProvider;
import com.example.softbackgrounds.motion.providers.RotationVectorProvider;


public class MotionProviderFactory {

    public static MotionProvider createProvider(Context context, MotionProviderType providerType) {
        switch (providerType) {
            case ROTATION_VECTOR:
                return new RotationVectorProvider(context);

            case GAME_ROTATION_VECTOR:
                return new GameRotationProvider(context);

            case ACCELEROMETER_MAGNETOMETER:
                return new AccelerometerMagnetometerProvider(context);

            case ACCELEROMETER:
                return new AccelerometerProvider(context);

            case NONE:
            default:
                return null;
        }
    }
}
