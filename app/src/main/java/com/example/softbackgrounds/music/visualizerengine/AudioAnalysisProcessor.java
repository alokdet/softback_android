package com.example.softbackgrounds.music.visualizerengine;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.audio.BaseAudioProcessor;
import androidx.media3.common.util.UnstableApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Observes PCM audio flowing through ExoPlayer.
 *
 * This processor does NOT modify the audio.
 * It only analyzes the samples and forwards them unchanged.
 */
@UnstableApi public class AudioAnalysisProcessor extends BaseAudioProcessor {

    private static final int FFT_SIZE = 2048;

    private int sampleRate;
    private int channelCount;

    private final BeatDetector beatDetector =
            new BeatDetector();

    private final float[] samples =
            new float[FFT_SIZE];

    private final float[] real =
            new float[FFT_SIZE];

    private final float[] imaginary =
            new float[FFT_SIZE];

    private int sampleIndex;

    private float amplitude;
    private float bass;
    private float mid;
    private float treble;

    private long lastFrameTimeMs;

    private final AudioAnalysisManager analysisManager;

    public AudioAnalysisProcessor(
            AudioAnalysisManager analysisManager
    ) {
        this.analysisManager = analysisManager;
    }

    @Override
    protected AudioFormat onConfigure(
            AudioFormat inputAudioFormat
    ) throws AudioProcessor.UnhandledAudioFormatException {

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw new AudioProcessor.UnhandledAudioFormatException(
                    "AudioAnalysisProcessor requires PCM 16-bit",
                    inputAudioFormat
            );
        }

        sampleRate = inputAudioFormat.sampleRate;
        channelCount = inputAudioFormat.channelCount;

        return inputAudioFormat;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {

        ByteBuffer buffer =
                inputBuffer
                        .slice()
                        .order(ByteOrder.nativeOrder());

        while (buffer.remaining() >=
                2 * channelCount) {

            float monoSample = 0f;

            for (int channel = 0;
                 channel < channelCount;
                 channel++) {

                short pcm = buffer.getShort();

                monoSample += pcm / 32768f;
            }

            monoSample /= channelCount;

            samples[sampleIndex++] =
                    monoSample;

            if (sampleIndex >= FFT_SIZE) {

                analyzeSamples();

                sampleIndex = 0;
            }
        }

        // Forward original audio unchanged.
        int remaining = inputBuffer.remaining();

        ByteBuffer output =
                replaceOutputBuffer(remaining);

        output.put(inputBuffer);

        output.flip();
    }

    private void analyzeSamples() {

        float rmsSum = 0f;

        for (int i = 0; i < FFT_SIZE; i++) {

            float sample = samples[i];

            rmsSum += sample * sample;

            // Hann window
            double window =
                    0.5 * (
                            1.0 -
                                    Math.cos(
                                            2.0 * Math.PI * i /
                                                    (FFT_SIZE - 1)
                                    )
                    );

            real[i] =
                    (float) (sample * window);

            imaginary[i] = 0f;
        }

        float rms =
                (float) Math.sqrt(
                        rmsSum / FFT_SIZE
                );

        float newAmplitude =
                clamp(rms * 4f);

        FastFourierTransform.transform(
                real,
                imaginary
        );

        float newBass =
                calculateBand(
                        20,
                        250
                );

        long now =
                System.currentTimeMillis();

        BeatDetector.BeatResult beatResult =
                beatDetector.analyze(
                        newBass,
                        now
                );

        float newMid =
                calculateBand(
                        250,
                        2000
                );

        float newTreble =
                calculateBand(
                        2000,
                        8000
                );

        // Smooth everything.
        amplitude =
                smooth(
                        amplitude,
                        newAmplitude,
                        0.25f
                );

        bass =
                smooth(
                        bass,
                        newBass,
                        0.20f
                );

        mid =
                smooth(
                        mid,
                        newMid,
                        0.20f
                );

        treble =
                smooth(
                        treble,
                        newTreble,
                        0.20f
                );

        publishFrame(
                beatResult.getBeat(),
                beatResult.getStrength()
        );
    }

    private float calculateBand(
            float minimumFrequency,
            float maximumFrequency
    ) {

        int minBin =
                Math.max(
                        1,
                        (int) (
                                minimumFrequency *
                                        FFT_SIZE /
                                        sampleRate
                        )
                );

        int maxBin =
                Math.min(
                        FFT_SIZE / 2,
                        (int) (
                                maximumFrequency *
                                        FFT_SIZE /
                                        sampleRate
                        )
                );

        float total = 0f;
        int count = 0;

        for (int bin = minBin;
             bin <= maxBin;
             bin++) {

            float magnitude =
                    FastFourierTransform.magnitude(
                            real[bin],
                            imaginary[bin]
                    );

            total += magnitude;
            count++;
        }

        if (count == 0) {
            return 0f;
        }

        float average =
                total / count;

        /*
         * FFT magnitude can be much larger than 1.
         * Log compression makes it usable as a
         * visualizer value.
         */
        float normalized =
                (float) (
                        Math.log1p(
                                average
                        ) / Math.log1p(100.0)
                );

        return clamp(normalized);
    }

    private void publishFrame(
            float beat,
            float beatStrength
    ) {

        long now =
                System.currentTimeMillis();

        if (now - lastFrameTimeMs < 16) {
            return;
        }

        lastFrameTimeMs = now;

        analysisManager.publishFrame(
                new AudioReactiveFrame(
                        now,
                        amplitude,
                        bass,
                        mid,
                        treble,
                        beat,
                        beatStrength
                )
        );
    }

    private float smooth(
            float oldValue,
            float newValue,
            float factor
    ) {
        return oldValue +
                (newValue - oldValue) * factor;
    }

    private float clamp(float value) {
        return Math.max(
                0f,
                Math.min(1f, value)
        );
    }

//    @Override
//    protected void onFlush(
//            AudioProcessor.StreamMetadata streamMetadata
//    ) {
//        resetState();
//    }

    @Override
    protected void onFlush() {
        super.onFlush();

        resetState();
    }

    @Override
    protected void onReset() {
        resetState();
    }

    private void resetState() {

        sampleIndex = 0;

        amplitude = 0f;
        bass = 0f;
        mid = 0f;
        treble = 0f;

        lastFrameTimeMs = 0;

        beatDetector.reset();

        analysisManager.reset();
    }
}
