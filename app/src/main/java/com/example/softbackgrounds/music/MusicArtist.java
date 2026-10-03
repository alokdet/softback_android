package com.example.softbackgrounds.music;

public class MusicArtist {

    private final long id;
    private final String name;
    private final int trackCount;
    private final int albumCount;

    public MusicArtist(
            long id,
            String name,
            int trackCount,
            int albumCount
    ) {
        this.id = id;
        this.name = name;
        this.trackCount = trackCount;
        this.albumCount = albumCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public int getAlbumCount() {
        return albumCount;
    }
}
