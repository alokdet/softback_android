package com.example.softbackgrounds.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicQueue {

    private final List<MusicTrack> tracks =
            new ArrayList<>();

    private int currentIndex = -1;

    public void setTracks(List<MusicTrack> newTracks) {

        tracks.clear();

        if (newTracks != null) {
            tracks.addAll(newTracks);
        }

        currentIndex =
                tracks.isEmpty() ? -1 : 0;
    }

    public void add(MusicTrack track) {

        if (track == null) {
            return;
        }

        tracks.add(track);

        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    public void addAll(List<MusicTrack> newTracks) {

        if (newTracks == null) {
            return;
        }

        for (MusicTrack track : newTracks) {
            add(track);
        }
    }

    public MusicTrack getCurrent() {

        if (currentIndex < 0 ||
                currentIndex >= tracks.size()) {
            return null;
        }

        return tracks.get(currentIndex);
    }

    public MusicTrack get(int index) {

        if (index < 0 ||
                index >= tracks.size()) {
            return null;
        }

        return tracks.get(index);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {

        if (index >= 0 &&
                index < tracks.size()) {

            currentIndex = index;
        }
    }

    public MusicTrack next() {

        if (tracks.isEmpty()) {
            return null;
        }

        currentIndex++;

        if (currentIndex >= tracks.size()) {
            currentIndex = 0;
        }

        return getCurrent();
    }

    public MusicTrack previous() {

        if (tracks.isEmpty()) {
            return null;
        }

        currentIndex--;

        if (currentIndex < 0) {
            currentIndex =
                    tracks.size() - 1;
        }

        return getCurrent();
    }

    public List<MusicTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    public int size() {
        return tracks.size();
    }

    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    public void clear() {

        tracks.clear();
        currentIndex = -1;
    }
}
