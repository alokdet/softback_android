package com.example.softbackgrounds.music.lyricsengine;

public interface LyricsListener {

    void onLyricsLoaded(Lyrics lyrics);

    void onLyricsError(Exception error);

    void onCurrentLineChanged(
            LyricsLine currentLine,
            LyricsLine nextLine
    );
}
