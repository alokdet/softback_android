package com.example.softbackgrounds.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlaylist {

    private final String id;
    private String name;
    private final List<String> trackIds;

    public MusicPlaylist(
            String id,
            String name
    ) {
        this.id = id;
        this.name = name;
        this.trackIds = new ArrayList<>();
    }

    public MusicPlaylist(
            String id,
            String name,
            List<String> trackIds
    ) {
        this.id = id;
        this.name = name;
        this.trackIds = new ArrayList<>();

        if (trackIds != null) {
            this.trackIds.addAll(trackIds);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTrackIds() {
        return Collections.unmodifiableList(trackIds);
    }

    public void addTrack(String trackId) {

        if (trackId == null) return;

        if (!trackIds.contains(trackId)) {
            trackIds.add(trackId);
        }
    }

    public void removeTrack(String trackId) {
        trackIds.remove(trackId);
    }

    public boolean contains(String trackId) {
        return trackIds.contains(trackId);
    }
}
