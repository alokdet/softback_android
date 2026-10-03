package com.example.softbackgrounds.music.lyricsengine;

public class LyricsWord {

    private final long startTimeMs;
    private final long endTimeMs;
    private final String text;

    public LyricsWord(
            long startTimeMs,
            long endTimeMs,
            String text
    ) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.text = text;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public String getText() {
        return text;
    }

    public boolean isActive(long positionMs) {
        return positionMs >= startTimeMs
                && positionMs < endTimeMs;
    }
}
