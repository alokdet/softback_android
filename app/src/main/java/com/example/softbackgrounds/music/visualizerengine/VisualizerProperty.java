package com.example.softbackgrounds.music.visualizerengine;

public class VisualizerProperty {

    private final AudioReactiveSource source;
    private final VisualizerAnimation animation;

    private final float intensity;
    private final float smoothing;

    private final float min;
    private final float max;

    public VisualizerProperty(
            AudioReactiveSource source,
            VisualizerAnimation animation,
            float intensity,
            float smoothing,
            float min,
            float max
    ) {
        this.source = source;
        this.animation = animation;
        this.intensity = intensity;
        this.smoothing = smoothing;
        this.min = min;
        this.max = max;
    }

    public AudioReactiveSource getSource() {
        return source;
    }

    public VisualizerAnimation getAnimation() {
        return animation;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getSmoothing() {
        return smoothing;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }
}
