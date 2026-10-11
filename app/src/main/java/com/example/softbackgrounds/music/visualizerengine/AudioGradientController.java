package com.example.softbackgrounds.music.visualizerengine;

import android.os.Handler;
import android.os.Looper;

import com.example.softbackgrounds.LiquidBlobView;

public final class AudioGradientController {

    private final LiquidBlobView liquidBlobView;
    private final AudioAnalysisManager audioAnalysisManager;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean running = false;

    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }

            AudioReactiveFrame frame =
                    audioAnalysisManager.getLatestFrame();

            if (frame != null) {
                liquidBlobView.setAudioData(
                        frame.getBass(),
                        frame.getMid(),
                        frame.getTreble()
                );
            }

            handler.postDelayed(this, 33);
        }
    };

    public AudioGradientController(
            LiquidBlobView liquidBlobView,
            AudioAnalysisManager audioAnalysisManager
    ) {
        this.liquidBlobView = liquidBlobView;
        this.audioAnalysisManager = audioAnalysisManager;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        handler.post(updateTask);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(updateTask);
    }
}
