package com.example.softbackgrounds.music;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

public class MusicLibraryObserver extends ContentObserver {

    public interface Listener {
        void onLibraryChanged();
    }

    private final Listener listener;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private boolean refreshScheduled;

    public MusicLibraryObserver(Listener listener) {
        super(new Handler(Looper.getMainLooper()));
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        scheduleRefresh();
    }

    private void scheduleRefresh() {

        if (refreshScheduled) return;

        refreshScheduled = true;

        mainHandler.postDelayed(() -> {

            refreshScheduled = false;

            if (listener != null) {
                listener.onLibraryChanged();
            }

        }, 500);
    }

    public static Uri getAudioUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }
}
