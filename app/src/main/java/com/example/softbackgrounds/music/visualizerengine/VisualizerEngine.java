package com.example.softbackgrounds.music.visualizerengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizerEngine {

    private final Map<Integer, Float> smoothedValues =
            new HashMap<>();

    public List<VisualizerOutput> calculate(
            String elementId,
            List<VisualizerProperty> properties,
            AudioReactiveFrame frame
    ) {

        List<VisualizerOutput> outputs =
                new ArrayList<>();

        if (elementId == null ||
                properties == null ||
                frame == null) {
            return outputs;
        }

        for (int i = 0;
             i < properties.size();
             i++) {

            VisualizerProperty property =
                    properties.get(i);

            if (property == null) {
                continue;
            }

            float input =
                    getSourceValue(
                            property.getSource(),
                            frame
                    );

            input =
                    clamp(input);

            float target =
                    property.getMin()
                            +
                            (
                                    property.getMax()
                                            -
                                            property.getMin()
                            )
                                    * input
                                    * property.getIntensity();

            String key =
                    elementId + ":" + i;

            float previous =
                    smoothedValues.containsKey(
                            key.hashCode()
                    )
                            ? smoothedValues.get(
                            key.hashCode()
                    )
                            : target;

            float smoothing =
                    clamp(
                            property.getSmoothing()
                    );

            float value =
                    previous
                            +
                            (
                                    target -
                                            previous
                            )
                                    * (
                                    1f -
                                            smoothing
                            );

            smoothedValues.put(
                    key.hashCode(),
                    value
            );

            outputs.add(
                    new VisualizerOutput(
                            property.getAnimation(),
                            value
                    )
            );
        }

        return outputs;
    }

    private float getSourceValue(
            AudioReactiveSource source,
            AudioReactiveFrame frame
    ) {

        if (source == null) {
            return 0f;
        }

        switch (source) {

            case AMPLITUDE:
                return frame.getAmplitude();

            case BASS:
                return frame.getBass();

            case MID:
                return frame.getMid();

            case TREBLE:
                return frame.getTreble();

            case BEAT:
                return frame.getBeat();

            case BEAT_STRENGTH:
                return frame.getBeatStrength();

            default:
                return 0f;
        }
    }

    public void clearElement(
            String elementId
    ) {

        if (elementId == null) {
            return;
        }

        String prefix =
                elementId + ":";

        smoothedValues.keySet().removeIf(
                key -> String.valueOf(key)
                        .startsWith(prefix)
        );
    }

    public void clear() {
        smoothedValues.clear();
    }

    private float clamp(float value) {
        return Math.max(
                0f,
                Math.min(1f, value)
        );
    }
}
