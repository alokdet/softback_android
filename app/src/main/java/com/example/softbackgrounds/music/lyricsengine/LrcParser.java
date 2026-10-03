package com.example.softbackgrounds.music.lyricsengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LrcParser {

    private static final Pattern TIMESTAMP_PATTERN =
            Pattern.compile(
                    "\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]"
            );

    private LrcParser() {
    }

    public static List<LyricsLine> parse(String lrc) {

        List<LyricsLine> result = new ArrayList<>();

        if (lrc == null || lrc.trim().isEmpty()) {
            return result;
        }

        String[] lines = lrc.split("\\r?\\n");

        for (String line : lines) {

            Matcher matcher = TIMESTAMP_PATTERN.matcher(line);

            List<Long> timestamps = new ArrayList<>();

            int contentStart = -1;

            while (matcher.find()) {

                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));

                String fraction = matcher.group(3);

                long milliseconds = 0;

                if (fraction != null) {

                    if (fraction.length() == 1) {
                        milliseconds =
                                Integer.parseInt(fraction) * 100L;
                    } else if (fraction.length() == 2) {
                        milliseconds =
                                Integer.parseInt(fraction) * 10L;
                    } else {
                        milliseconds =
                                Integer.parseInt(fraction.substring(0, 3));
                    }
                }

                long timestamp =
                        minutes * 60_000L
                                + seconds * 1_000L
                                + milliseconds;

                timestamps.add(timestamp);

                if (contentStart == -1) {
                    contentStart = matcher.end();
                }
            }

            if (timestamps.isEmpty()) {
                continue;
            }

            String text =
                    line.substring(contentStart).trim();

            for (long timestamp : timestamps) {
                result.add(
                        new LyricsLine(
                                timestamp,
                                text
                        )
                );
            }
        }

        Collections.sort(
                result,
                Comparator.comparingLong(
                        LyricsLine::getStartTimeMs
                )
        );

        return result;
    }
}
