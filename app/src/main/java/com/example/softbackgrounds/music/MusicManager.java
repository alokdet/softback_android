
package com.example.softbackgrounds.music;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public final class MusicManager {
    private static volatile MusicManager instance;
    private final MusicPlayer musicPlayer;
    private final MusicQueue musicQueue = new MusicQueue();
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();
    private boolean initialized;
    private boolean playerListenerAttached;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            for (Listener listener : listeners) {
                listener.onPlaybackChanged(isPlaying);
            }
        }
        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason) {
            updateQueueIndex();
            MusicTrack track = findTrack(mediaItem);

            for (Listener listener : listeners) {
                listener.onTrackChanged(track);
            }
        }
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            for (Listener listener : listeners) {
                listener.onPlaybackStateChanged(playbackState);
            }
        }
        @Override
        public void onPlayerError(PlaybackException error) {
            for (Listener listener : listeners) {
                listener.onPlaybackError(error.getMessage());
            }
        }
    };

    private MusicManager(Context context) {
        musicPlayer = new MusicPlayer(context.getApplicationContext());
    }

    public static MusicManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MusicManager.class) {
                if (instance == null) {
                    instance = new MusicManager(context);
                }
            }
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;

        initialized = true;
        musicPlayer.connect();

        musicPlayer.setOnConnectedListener(() -> {
            if (playerListenerAttached) return;

            MediaController controller = musicPlayer.getController();

            if (controller != null) {
                controller.addListener(playerListener);
                playerListenerAttached = true;
            }
        });
    }

    public void setQueue(List<MusicTrack> tracks) {
        if (tracks == null) return;

        musicQueue.setTracks(tracks);
        musicPlayer.setQueue(tracks);

        for (Listener listener : listeners) {
            listener.onQueueChanged(musicQueue.size());
        }
    }

    public void playAt(int index) {
        musicPlayer.playAt(index);
        musicQueue.setCurrentIndex(index);
    }

    public void play(MusicTrack track) {
        if (track == null) return;

        musicQueue.setTracks(java.util.Collections.singletonList(track));
        musicPlayer.play(track);

        for (Listener listener : listeners) {
            listener.onQueueChanged(musicQueue.size());
        }
    }

    public void pause() {
        musicPlayer.pause();
    }

    public void resume() {
        musicPlayer.resume();
    }

    public void togglePlayPause() {
        musicPlayer.togglePlayPause();
    }

    public void next() {
        musicPlayer.next();
    }

    public void previous() {
        musicPlayer.previous();
    }

    public void seekTo(long positionMs) {
        musicPlayer.seekTo(positionMs);
    }

    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    public long getCurrentPosition() {
        return musicPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return musicPlayer.getDuration();
    }

    public MusicTrack getCurrentTrack() {
        MediaItem item = musicPlayer.getCurrentMediaItem();
        return findTrack(item);
    }

    public List<MusicTrack> getQueue() {
        return musicQueue.getTracks();
    }

    public void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private MusicTrack findTrack(MediaItem item) {
        if (item == null) return null;

        String mediaId = item.mediaId;

        for (MusicTrack track : musicQueue.getTracks()) {
            if (track.getId().equals(mediaId)) {
                return track;
            }
        }
        return null;
    }

    private void updateQueueIndex() {
        int index = musicPlayer.getCurrentMediaItemIndex();

        if (index >= 0 && index < musicQueue.size()) {
            musicQueue.setCurrentIndex(index);
        }
    }

    public interface Listener {
        default void onTrackChanged(MusicTrack track) {}
        default void onPlaybackChanged(boolean isPlaying) {}
        default void onPlaybackStateChanged(int state) {}
        default void onQueueChanged(int queueSize) {}
        default void onPlaybackError(String message) {}
    }
}
