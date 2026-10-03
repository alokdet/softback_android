package com.example.softbackgrounds.music.lyricsengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lyrics {

    private final String plainLyrics;
    private final List<LyricsLine> syncedLines;
    private final String source;

    public Lyrics(
            String plainLyrics,
            List<LyricsLine> syncedLines,
            String source
    ) {
        this.plainLyrics = plainLyrics;
        this.syncedLines =
                syncedLines == null
                        ? new ArrayList<>()
                        : new ArrayList<>(syncedLines);
        this.source = source;
    }

    public String getPlainLyrics() {
        return plainLyrics;
    }

    public List<LyricsLine> getSyncedLines() {
        return Collections.unmodifiableList(syncedLines);
    }

    public String getSource() {
        return source;
    }

    public boolean hasPlainLyrics() {
        return plainLyrics != null && !plainLyrics.trim().isEmpty();
    }

    public boolean hasSyncedLyrics() {
        return !syncedLines.isEmpty();
    }
}
