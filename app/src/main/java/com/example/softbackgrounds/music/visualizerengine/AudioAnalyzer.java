package com.example.softbackgrounds.music.visualizerengine;

import java.nio.ByteBuffer;

public interface AudioAnalyzer {

    void configure(
            int sampleRate,
            int channelCount,
            int encoding
    );

    void process(ByteBuffer buffer);

    AudioReactiveFrame getLatestFrame();

    void reset();

    void release();
}
