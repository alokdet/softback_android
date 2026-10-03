package com.example.softbackgrounds.music.lyricsengine;

import android.content.Context;

import com.example.softbackgrounds.music.MusicTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompositeLyricsRepository
        implements LyricsRepository {

    private final CachedLyricsRepository cache;
    private final LrcLibLyricsRepository network;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public CompositeLyricsRepository(
            Context context
    ) {

        cache =
                new CachedLyricsRepository(
                        context.getApplicationContext()
                );

        network =
                new LrcLibLyricsRepository();
    }

    @Override
    public void getLyrics(
            MusicTrack track,
            Callback callback
    ) {

        if (track == null) {

            callback.onError(
                    new IllegalArgumentException(
                            "Track cannot be null"
                    )
            );

            return;
        }

        executor.execute(() -> {

            Lyrics cached =
                    cache.get(track.getId());

            if (cached != null) {

                callback.onSuccess(cached);
                return;
            }

            network.getLyrics(
                    track,
                    new Callback() {

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
        });
    }

    public void clearCache() {
        cache.clear();
    }

    public void shutdown() {

        executor.shutdownNow();

        network.shutdown();
    }
}
