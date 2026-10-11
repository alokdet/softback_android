package com.example.softbackgrounds.music.visualizerengine;

public final class AudioReactiveFrame {

    private final long timestampMs;

    private final float amplitude;
    private final float bass;
    private final float mid;
    private final float treble;

    private final float beat;
    private final float beatStrength;

    public AudioReactiveFrame(
            long timestampMs,
            float amplitude,
            float bass,
            float mid,
            float treble,
            float beat,
            float beatStrength
    ) {
        this.timestampMs = timestampMs;
        this.amplitude = amplitude;
        this.bass = bass;
        this.mid = mid;
        this.treble = treble;
        this.beat = beat;
        this.beatStrength = beatStrength;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getBass() {
        return bass;
    }

    public float getMid() {
        return mid;
    }

    public float getTreble() {
        return treble;
    }

    public float getBeat() {
        return beat;
    }

    public float getBeatStrength() {
        return beatStrength;
    }
}
