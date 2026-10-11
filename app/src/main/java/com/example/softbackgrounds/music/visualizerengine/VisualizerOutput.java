package com.example.softbackgrounds.music.visualizerengine;

public class VisualizerOutput {

    private final VisualizerAnimation animation;
    private final float value;

    public VisualizerOutput(
            VisualizerAnimation animation,
            float value
    ) {
        this.animation = animation;
        this.value = value;
    }

    public VisualizerAnimation getAnimation() {
        return animation;
    }

    public float getValue() {
        return value;
    }
}
