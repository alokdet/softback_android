package com.example.softbackgrounds.music;

import android.net.Uri;

public class MusicAlbum {

    private final long id;
    private final String name;
    private final String artist;
    private final Uri artworkUri;
    private final int trackCount;

    public MusicAlbum(
            long id,
            String name,
            String artist,
            Uri artworkUri,
            int trackCount
    ) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.artworkUri = artworkUri;
        this.trackCount = trackCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public int getTrackCount() {
        return trackCount;
    }
}
