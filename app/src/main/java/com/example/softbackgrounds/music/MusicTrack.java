package com.example.softbackgrounds.music;

import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

public class MusicTrack {

    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private final Uri uri;
    private final Uri artworkUri;
    private final long duration;

    public MusicTrack(
            String id,
            String title,
            String artist,
            String album,
            Uri uri,
            Uri artworkUri,
            long duration
    ) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.duration = duration;
    }

    public MediaItem toMediaItem() {

        MediaMetadata metadata =
                new MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setAlbumTitle(album)
                        .setArtworkUri(artworkUri)
                        .build();

        return new MediaItem.Builder()
                .setMediaId(id)
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public long getDuration() {
        return duration;
    }
}
