package com.example.softbackgrounds.music.lyricsengine;

import com.example.softbackgrounds.music.MusicTrack;

public interface LyricsRepository {

    void getLyrics(
            MusicTrack track,
            Callback callback
    );

    interface Callback {

        void onSuccess(Lyrics lyrics);

        void onError(Exception error);
    }
}
