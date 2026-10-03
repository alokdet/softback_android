package com.example.softbackgrounds.music;

import android.content.ComponentName;
import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {

    private final Context context;

    private MediaController controller;
    private ListenableFuture<MediaController> controllerFuture;

    public MusicPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    public void connect() {

        SessionToken sessionToken =
                new SessionToken(
                        context,
                        new ComponentName(
                                context,
                                MusicService.class
                        )
                );

        controllerFuture =
                new MediaController.Builder(
                        context,
                        sessionToken
                ).buildAsync();

        controllerFuture.addListener(
                () -> {
                    try {
                        controller = controllerFuture.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                context.getMainExecutor()
        );
    }

    public boolean isConnected() {
        return controller != null;
    }

    public void setQueue(List<MusicTrack> tracks) {

        if (controller == null || tracks == null ||
                tracks.isEmpty()) {
            return;
        }

        List<MediaItem> mediaItems = new ArrayList<>();

        for (MusicTrack track : tracks) {
            mediaItems.add(track.toMediaItem());
        }

        controller.setMediaItems(mediaItems);
        controller.prepare();
    }

    public void playAt(int index) {

        if (controller == null) {
            return;
        }

        if (index < 0 ||
                index >= controller.getMediaItemCount()) {
            return;
        }

        controller.seekToDefaultPosition(index);
        controller.play();
    }

    public void setOnConnectedListener(Runnable listener) {
        if (controller != null) {
            listener.run();
            return;
        }

        controllerFuture.addListener(() -> {
            try {
                controller = controllerFuture.get();
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }}, context.getMainExecutor());
    }

    public void play(MusicTrack track) {

        if (controller == null || track == null) {
            return;
        }

        controller.setMediaItem(
                track.toMediaItem()
        );

        controller.prepare();
        controller.play();
    }

    public void pause() {

        if (controller != null) {
            controller.pause();
        }
    }

    public void resume() {

        if (controller != null) {
            controller.play();
        }
    }

    public void togglePlayPause() {

        if (controller == null) {
            return;
        }

        if (controller.isPlaying()) {
            controller.pause();
        } else {
            controller.play();
        }
    }

    public void next() {

        if (controller != null) {
            controller.seekToNextMediaItem();
        }
    }

    public void previous() {

        if (controller != null) {
            controller.seekToPreviousMediaItem();
        }
    }

    public void seekTo(long positionMs) {

        if (controller != null) {
            controller.seekTo(positionMs);
        }
    }

    public long getCurrentPosition() {

        if (controller == null) {
            return 0;
        }

        return controller.getCurrentPosition();
    }

    public long getDuration() {

        if (controller == null) {
            return 0;
        }

        return controller.getDuration();
    }

    public boolean isPlaying() {

        return controller != null &&
                controller.isPlaying();
    }

    public MediaItem getCurrentMediaItem() {

        if (controller == null) {
            return null;
        }

        return controller.getCurrentMediaItem();
    }

    public int getCurrentMediaItemIndex() {

        if (controller == null) {
            return -1;
        }

        return controller.getCurrentMediaItemIndex();
    }

    public MediaController getController() {
        return controller;
    }

    public void release() {

        if (controllerFuture != null) {
            MediaController.releaseFuture(
                    controllerFuture
            );

            controllerFuture = null;
        }

        controller = null;
    }
}
