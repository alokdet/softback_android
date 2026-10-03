package com.example.softbackgrounds.music.lyricsengine;

import android.content.Context;

import com.example.softbackgrounds.music.MusicTrack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LyricsManager {

    private static volatile LyricsManager instance;

    private final CompositeLyricsRepository repository;
    private final LyricsSynchronizer synchronizer;

    private final Map<String, Lyrics> cache =
            new ConcurrentHashMap<>();

    private LyricsManager(Context context) {

        repository =
                new CompositeLyricsRepository(context);

        synchronizer =
                new LyricsSynchronizer();
    }

    public static LyricsManager getInstance(
            Context context
    ) {

        if (instance == null) {

            synchronized (LyricsManager.class) {

                if (instance == null) {

                    instance =
                            new LyricsManager(
                                    context.getApplicationContext()
                            );
                }
            }
        }

        return instance;
    }

    public void getLyrics(
            MusicTrack track,
            LyricsRepository.Callback callback
    ) {

        if (track == null) {
            callback.onError(
                    new IllegalArgumentException(
                            "Track cannot be null"
                    )
            );
            return;
        }

        Lyrics cached =
                cache.get(track.getId());

        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        repository.getLyrics(
                track,
                new LyricsRepository.Callback() {

                    @Override
                    public void onSuccess(
                            Lyrics lyrics
                    ) {

                        cache.put(
                                track.getId(),
                                lyrics
                        );

                        callback.onSuccess(
                                lyrics
                        );
                    }

                    @Override
                    public void onError(
                            Exception error
                    ) {

                        callback.onError(error);
                    }
                }
        );
    }

    public LyricsLine getCurrentLine(
            Lyrics lyrics,
            long positionMs
    ) {

        return synchronizer.getCurrentLine(
                lyrics,
                positionMs
        );
    }

    public int getCurrentLineIndex(
            Lyrics lyrics,
            long positionMs
    ) {
        return synchronizer.findCurrentLineIndex(
                lyrics,
                positionMs
        );
    }

    public void clearCache() {
        cache.clear();
        repository.clearCache();
    }

    public void clearTrackCache(
            MusicTrack track
    ) {
        if (track == null) {
            return;
        }

        cache.remove(track.getId());
    }

    public LyricsWord getCurrentWord(
            Lyrics lyrics,
            long positionMs
    ) {

        LyricsLine line =
                getCurrentLine(
                        lyrics,
                        positionMs
                );

        if (line == null) {
            return null;
        }

        return synchronizer.getCurrentWord(
                line,
                positionMs
        );
    }

    public LyricsLine getNextLine(
            Lyrics lyrics,
            long positionMs
    ) {

        return synchronizer.getNextLine(
                lyrics,
                positionMs
        );
    }

    public void clearMemoryCache() {
        cache.clear();
    }

    public void shutdown() {
        repository.shutdown();
        cache.clear();
    }
}
