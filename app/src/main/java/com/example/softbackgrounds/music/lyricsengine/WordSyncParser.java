package com.example.softbackgrounds.music.lyricsengine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WordSyncParser {

    /*
     * Supports timestamp formats such as:
     *
     * [00:12.50]
     * [00:12:500]
     *
     * The actual word-level format can vary by
     * lyrics provider, so this parser intentionally
     * remains separate from the normal LRC parser.
     */

    private static final Pattern TIMESTAMP =
            Pattern.compile(
                    "\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]"
            );

    private WordSyncParser() {
    }

    public static List<LyricsWord> parseWords(
            String text,
            long lineStartMs,
            long lineEndMs
    ) {

        List<LyricsWord> words =
                new ArrayList<>();

        if (text == null ||
                text.trim().isEmpty()) {
            return words;
        }

        /*
         * Generic fallback:
         *
         * If the source doesn't provide individual
         * timestamps, distribute the words across
         * the line duration.
         *
         * This is NOT true word synchronization.
         * It simply gives the future UI a useful
         * timing model.
         */

        String[] parts =
                text.trim().split("\\s+");

        if (parts.length == 0) {
            return words;
        }

        long duration =
                Math.max(
                        1,
                        lineEndMs - lineStartMs
                );

        long wordDuration =
                duration / parts.length;

        for (int i = 0;
             i < parts.length;
             i++) {

            long start =
                    lineStartMs
                            + i * wordDuration;

            long end;

            if (i == parts.length - 1) {
                end = lineEndMs;
            } else {
                end =
                        lineStartMs
                                + (i + 1)
                                * wordDuration;
            }

            words.add(
                    new LyricsWord(
                            start,
                            end,
                            parts[i]
                    )
            );
        }

        return words;
    }
}
