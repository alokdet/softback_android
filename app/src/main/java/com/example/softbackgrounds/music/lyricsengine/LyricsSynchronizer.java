package com.example.softbackgrounds.music.lyricsengine;

import java.util.List;

public class LyricsSynchronizer {

    public int findCurrentLineIndex(
            Lyrics lyrics,
            long positionMs
    ) {

        if (lyrics == null ||
                !lyrics.hasSyncedLyrics()) {
            return -1;
        }

        List<LyricsLine> lines =
                lyrics.getSyncedLines();

        int low = 0;
        int high = lines.size() - 1;

        int result = -1;

        while (low <= high) {

            int middle =
                    low + (high - low) / 2;

            long start =
                    lines.get(middle)
                            .getStartTimeMs();

            if (start <= positionMs) {

                result = middle;
                low = middle + 1;

            } else {

                high = middle - 1;
            }
        }

        return result;
    }

    public LyricsLine getCurrentLine(
            Lyrics lyrics,
            long positionMs
    ) {

        int index =
                findCurrentLineIndex(
                        lyrics,
                        positionMs
                );

        if (index < 0) {
            return null;
        }

        return lyrics.getSyncedLines().get(index);
    }

    public LyricsWord getCurrentWord(
            LyricsLine line,
            long positionMs
    ) {

        if (line == null ||
                !line.hasWordSync()) {
            return null;
        }

        List<LyricsWord> words =
                line.getWords();

        int low = 0;
        int high = words.size() - 1;

        while (low <= high) {

            int middle =
                    low + (high - low) / 2;

            LyricsWord word =
                    words.get(middle);

            if (positionMs < word.getStartTimeMs()) {

                high = middle - 1;

            } else if (
                    positionMs >= word.getEndTimeMs()
            ) {

                low = middle + 1;

            } else {

                return word;
            }
        }

        return null;
    }

    public LyricsLine getNextLine(
            Lyrics lyrics,
            long positionMs
    ) {

        int index =
                findCurrentLineIndex(
                        lyrics,
                        positionMs
                );

        int nextIndex = index + 1;

        if (nextIndex >=
                lyrics.getSyncedLines().size()) {

            return null;
        }

        return lyrics.getSyncedLines()
                .get(nextIndex);
    }
}
