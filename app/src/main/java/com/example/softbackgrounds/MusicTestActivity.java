package com.example.softbackgrounds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softbackgrounds.music.LocalMusicRepository;
import com.example.softbackgrounds.music.MusicAdapter;
import com.example.softbackgrounds.music.MusicLibraryObserver;
import com.example.softbackgrounds.music.MusicManager;
import com.example.softbackgrounds.music.MusicPermissionHelper;
import com.example.softbackgrounds.music.MusicSortOrder;
import com.example.softbackgrounds.music.MusicTrack;
import com.example.softbackgrounds.music.NowPlayingActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicTestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MusicAdapter adapter;

    private LocalMusicRepository repository;
    private MusicManager musicManager;

    private MusicLibraryObserver musicLibraryObserver;

    private final ExecutorService musicExecutor =
            Executors.newSingleThreadExecutor();

    private List<MusicTrack> loadedTracks = new ArrayList<>();

    /*
     * Audio permission
     */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            loadMusic();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Music permission is required.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    /*
     * Notification permission
     */
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        // Nothing else required here.
                        // Media3 handles the playback notification.
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {

            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.DarkTheme_SoftBackgrounds);
                getWindow().setNavigationBarColor(Color.BLACK);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.LightTheme_SoftBackgrounds);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setNavigationBarColor(Color.WHITE);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.music_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        repository = new LocalMusicRepository(this);

        musicManager = MusicManager.getInstance(this);
        musicManager.initialize();

        adapter = new MusicAdapter(this, (track, position) -> {
            if (loadedTracks == null || position < 0 || position >= loadedTracks.size()) {
                return;
            }
            musicManager.setQueue(loadedTracks);
            musicManager.playAt(position);
            startActivity(new Intent(getApplicationContext(), NowPlayingActivity.class));
        });

        recyclerView.setAdapter(adapter);

        /*
         * Observe MediaStore changes.
         */
        musicLibraryObserver = new MusicLibraryObserver(this::loadMusic);

        getContentResolver().registerContentObserver(MusicLibraryObserver.getAudioUri(), true, musicLibraryObserver);

        /*
         * Ask for notification permission where required.
         */
        requestNotificationPermission();

        /*
         * Check audio permission and load music.
         */
        checkPermission();
    }

    private void checkPermission() {
        if (MusicPermissionHelper.hasPermission(this)) {
            loadMusic();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * Loads all local music from MediaStore.
     */
    private void loadMusic() {
        musicExecutor.execute(() -> {
            List<MusicTrack> tracks = repository.getAllTracks();

            if (tracks == null) {
                tracks = new ArrayList<>();
            }

            List<MusicTrack> finalTracks = new ArrayList<>(tracks);
            loadedTracks = finalTracks;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                adapter.submitList(new ArrayList<>(finalTracks));

                //The queue must contain exactly the same tracks and ordering as the RecyclerView.
                musicManager.setQueue(finalTracks);
            });
        });
    }

    /**
     * Loads recently added music.
     *
     * Can be used later for a "Recently Added" section.
     */
    private void loadRecentlyAdded() {
        musicExecutor.execute(() -> {
            List<MusicTrack> recent = repository.getRecentlyAdded(50);

            if (recent == null) {
                recent = new ArrayList<>();
            }

            List<MusicTrack> finalRecent = new ArrayList<>(recent);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                loadedTracks = finalRecent;
                adapter.submitList(new ArrayList<>(finalRecent));
                musicManager.setQueue(finalRecent);
            });
        });
    }

    /**
     * Search local music.
     */
    private void searchMusic(String query) {
        musicExecutor.execute(() -> {
            List<MusicTrack> results = repository.searchTracks(query);

            if (results == null) {
                results = new ArrayList<>();
            }

            List<MusicTrack> finalResults = new ArrayList<>(results);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                loadedTracks = finalResults;
                adapter.submitList(new ArrayList<>(finalResults));
                musicManager.setQueue(finalResults);
            });
        });
    }

    /**
     * Sort local music.
     */
    private void sortMusic(MusicSortOrder order) {
        musicExecutor.execute(() -> {
            List<MusicTrack> results = repository.getAllTracks(order);

            if (results == null) {
                results = new ArrayList<>();
            }

            List<MusicTrack> finalResults = new ArrayList<>(results);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                loadedTracks = finalResults;
                adapter.submitList(new ArrayList<>(finalResults));
                musicManager.setQueue(finalResults);
            });
        });
    }

    /**
     * Request notification permission on Android 13+.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Reload music when the MediaStore changes.
     */
    private void onMusicLibraryChanged() {
        if (MusicPermissionHelper.hasPermission(this)) {
            loadMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
         * Stop observing MediaStore.
         */
        if (musicLibraryObserver != null) {

            getContentResolver().unregisterContentObserver(musicLibraryObserver);
            musicLibraryObserver = null;
        }

        /*
         * Stop background database/MediaStore work.
         *
         * IMPORTANT:
         * We do NOT release MusicManager here.
         *
         * MusicManager owns the background playback system,
         * so music can continue playing after this Activity
         * is closed.
         */
        musicExecutor.shutdownNow();
    }
}
