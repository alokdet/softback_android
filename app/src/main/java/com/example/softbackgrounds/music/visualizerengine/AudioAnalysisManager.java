package com.example.softbackgrounds.music.visualizerengine;

import android.content.Context;

public class AudioAnalysisManager {

    private static volatile AudioAnalysisManager instance;

    private final Context context;

    private volatile AudioReactiveFrame latestFrame;

    private AudioAnalysisManager(Context context) {
        this.context = context.getApplicationContext();

        latestFrame = new AudioReactiveFrame(
                System.currentTimeMillis(),
                0f,
                0f,
                0f,
                0f,
                0f,
                0f
        );
    }

    public static AudioAnalysisManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AudioAnalysisManager.class) {
                if (instance == null) {
                    instance = new AudioAnalysisManager(context);
                }
            }
        }

        return instance;
    }

    public AudioReactiveFrame getLatestFrame() {
        return latestFrame;
    }

    void publishFrame(AudioReactiveFrame frame) {
        if (frame != null) {
            latestFrame = frame;
        }
    }

    public void reset() {
        latestFrame = new AudioReactiveFrame(
                System.currentTimeMillis(),
                0f,
                0f,
                0f,
                0f,
                0f,
                0f
        );
    }
}
