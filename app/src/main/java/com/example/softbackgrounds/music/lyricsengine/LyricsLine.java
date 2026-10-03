package com.example.softbackgrounds.music.lyricsengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricsLine {

    private final long startTimeMs;
    private final String text;
    private final List<LyricsWord> words;

    public LyricsLine(
            long startTimeMs,
            String text
    ) {
        this(
                startTimeMs,
                text,
                new ArrayList<>()
        );
    }

    public LyricsLine(
            long startTimeMs,
            String text,
            List<LyricsWord> words
    ) {
        this.startTimeMs = startTimeMs;
        this.text = text;

        this.words =
                words == null
                        ? new ArrayList<>()
                        : new ArrayList<>(words);
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public String getText() {
        return text;
    }

    public List<LyricsWord> getWords() {
        return Collections.unmodifiableList(words);
    }

    public boolean hasWordSync() {
        return !words.isEmpty();
    }
}
