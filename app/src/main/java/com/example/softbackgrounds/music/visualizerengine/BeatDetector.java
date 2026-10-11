package com.example.softbackgrounds.music.visualizerengine;

public class BeatDetector {

    private static final int HISTORY_SIZE = 43;

    private final float[] energyHistory =
            new float[HISTORY_SIZE];

    private int historyIndex = 0;
    private int historyCount = 0;

    private long lastBeatTimeMs = 0;

    private float beatStrength = 0f;

    public synchronized BeatResult analyze(
            float bassEnergy,
            long timestampMs
    ) {

        float average = getAverageEnergy();

        float variance =
                getVariance(average);

        float standardDeviation =
                (float) Math.sqrt(variance);

        /*
         * Adaptive threshold.
         *
         * A beat is detected when current bass
         * energy rises sufficiently above the
         * recent average.
         */
        float threshold =
                average +
                        standardDeviation * 1.35f;

        boolean beat =
                bassEnergy > threshold &&
                        bassEnergy > 0.12f &&
                        timestampMs - lastBeatTimeMs > 120;

        float strength = 0f;

        if (beat) {

            lastBeatTimeMs =
                    timestampMs;

            float difference =
                    bassEnergy - average;

            float denominator =
                    Math.max(
                            0.05f,
                            threshold - average
                    );

            strength =
                    clamp(
                            difference /
                                    denominator
                    );
        }

        /*
         * Add current energy to history AFTER
         * calculating the threshold.
         */
        energyHistory[historyIndex] =
                bassEnergy;

        historyIndex =
                (historyIndex + 1)
                        % HISTORY_SIZE;

        if (historyCount < HISTORY_SIZE) {
            historyCount++;
        }

        /*
         * Decay beat strength naturally.
         */
        beatStrength *= 0.82f;

        if (beat) {
            beatStrength =
                    Math.max(
                            beatStrength,
                            strength
                    );
        }

        return new BeatResult(
                beat ? 1f : 0f,
                clamp(beatStrength)
        );
    }

    private float getAverageEnergy() {

        if (historyCount == 0) {
            return 0f;
        }

        float total = 0f;

        for (int i = 0;
             i < historyCount;
             i++) {

            total += energyHistory[i];
        }

        return total / historyCount;
    }

    private float getVariance(
            float average
    ) {

        if (historyCount == 0) {
            return 0f;
        }

        float total = 0f;

        for (int i = 0;
             i < historyCount;
             i++) {

            float difference =
                    energyHistory[i] -
                            average;

            total +=
                    difference *
                            difference;
        }

        return total / historyCount;
    }

    public synchronized void reset() {

        for (int i = 0;
             i < energyHistory.length;
             i++) {

            energyHistory[i] = 0f;
        }

        historyIndex = 0;
        historyCount = 0;

        lastBeatTimeMs = 0;
        beatStrength = 0f;
    }

    private float clamp(float value) {

        return Math.max(
                0f,
                Math.min(1f, value)
        );
    }

    public static final class BeatResult {

        private final float beat;
        private final float strength;

        public BeatResult(
                float beat,
                float strength
        ) {
            this.beat = beat;
            this.strength = strength;
        }

        public float getBeat() {
            return beat;
        }

        public float getStrength() {
            return strength;
        }
    }
}
