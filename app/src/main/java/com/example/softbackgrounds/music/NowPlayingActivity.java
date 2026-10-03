package com.example.softbackgrounds.music;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.softbackgrounds.R;
import com.example.softbackgrounds.music.lyricsengine.Lyrics;
import com.example.softbackgrounds.music.lyricsengine.LyricsLine;
import com.example.softbackgrounds.music.lyricsengine.LyricsManager;
import com.example.softbackgrounds.music.lyricsengine.LyricsRepository;

import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NowPlayingActivity extends AppCompatActivity {

    private MusicManager musicManager;
    private LyricsManager lyricsManager;

    private AppCompatImageView albumArt;
    private AppCompatTextView title;
    private AppCompatTextView artist;
    private AppCompatTextView currentTime;
    private AppCompatTextView totalTime;

    private SeekBar seekBar;

    private AppCompatImageView previous;
    private AppCompatImageView playPause;
    private AppCompatImageView next;

    private Lyrics currentLyrics;

    private AppCompatTextView lyricsStatus;
    private AppCompatTextView previousLyric;
    private AppCompatTextView currentLyric;
    private AppCompatTextView nextLyric;

    private String lyricsTrackId = null;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ExecutorService artworkExecutor = Executors.newSingleThreadExecutor();

    private boolean isUserSeeking = false;
    private String displayedTrackId = null;

    // ---------------------------------------------------------
    // Progress updater
    // ---------------------------------------------------------

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {

            updateProgress();

            handler.postDelayed(this, 500);
        }
    };

    // ---------------------------------------------------------
    // Music listener
    // ---------------------------------------------------------

    private final MusicManager.Listener musicListener =
            new MusicManager.Listener() {

                @Override
                public void onTrackChanged(MusicTrack track) {

                    runOnUiThread(() -> {

                        updateTrackInfo(track);

                        // Load lyrics for the new track.
                        loadLyricsForCurrentTrack();
                    });
                }

                @Override
                public void onPlaybackChanged(boolean isPlaying) {

                    runOnUiThread(() ->
                            updatePlayPauseButton(isPlaying)
                    );
                }

                @Override
                public void onPlaybackError(String message) {

                    runOnUiThread(() -> {

                        artist.setText(
                                message != null
                                        ? message
                                        : "Playback error"
                        );
                    });
                }
            };

    // ---------------------------------------------------------
    // Activity lifecycle
    // ---------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int nightModeFlags =
                getResources()
                        .getConfiguration()
                        .uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {

            case Configuration.UI_MODE_NIGHT_YES:

                setTheme(R.style.DarkTheme_SoftBackgrounds);

                getWindow().setNavigationBarColor(Color.BLACK);

                break;

            case Configuration.UI_MODE_NIGHT_NO:

                setTheme(R.style.LightTheme_SoftBackgrounds);

                getWindow()
                        .getDecorView()
                        .setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        );

                getWindow().setNavigationBarColor(Color.WHITE);

                break;
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_now_playing);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        initializeMusicManager();
        initializeLyrics();
        initializeControls();
    }

    // Views
    private void initializeViews() {
        albumArt = findViewById(R.id.albumArt);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        previous = findViewById(R.id.previous);
        playPause = findViewById(R.id.playPause);
        next = findViewById(R.id.next);
        previousLyric = findViewById(R.id.previous_line);
        currentLyric = findViewById(R.id.current_line);
        nextLyric = findViewById(R.id.next_line);
        lyricsStatus = findViewById(R.id.lyrics_status);
    }

    // ---------------------------------------------------------
    // Music manager
    // ---------------------------------------------------------

    private void initializeMusicManager() {
        musicManager = MusicManager.getInstance(this);
        musicManager.initialize();
    }

    // ---------------------------------------------------------
    // Lyrics manager
    // ---------------------------------------------------------

    private void initializeLyrics() {
        lyricsManager = LyricsManager.getInstance(this);
        clearLyrics();
    }

    // Controls
    private void initializeControls() {

        previous.setOnClickListener(
                v -> musicManager.previous()
        );

        next.setOnClickListener(
                v -> musicManager.next()
        );

        playPause.setOnClickListener(v -> {

            if (musicManager.isPlaying()) {

                musicManager.pause();

            } else {

                musicManager.resume();
            }
        });

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        if (fromUser) {

                            currentTime.setText(
                                    formatTime(progress)
                            );

                            // Update lyrics while manually
                            // dragging the seek bar.
                            updateLyrics(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {

                        isUserSeeking = true;
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {

                        musicManager.seekTo(
                                seekBar.getProgress()
                        );

                        isUserSeeking = false;

                        updateLyrics(
                                seekBar.getProgress()
                        );
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // Start
    // ---------------------------------------------------------

    @Override
    protected void onStart() {

        super.onStart();

        musicManager.addListener(
                musicListener
        );

        MusicTrack currentTrack =
                musicManager.getCurrentTrack();

        updateTrackInfo(currentTrack);

        updatePlayPauseButton(
                musicManager.isPlaying()
        );

        // Load lyrics even when the song was already playing
        // before NowPlayingActivity opened.
        loadLyricsForCurrentTrack();

        handler.post(progressUpdater);
    }

    // ---------------------------------------------------------
    // Stop
    // ---------------------------------------------------------

    @Override
    protected void onStop() {

        super.onStop();

        musicManager.removeListener(
                musicListener
        );

        handler.removeCallbacks(
                progressUpdater
        );
    }

    // ---------------------------------------------------------
    // Track information
    // ---------------------------------------------------------

    private void updateTrackInfo(
            MusicTrack track
    ) {

        if (track == null) {

            displayedTrackId = null;

            title.setText(
                    "Nothing playing"
            );

            artist.setText(
                    "Select a song to start listening"
            );

            albumArt.setImageResource(
                    R.drawable.ic_music_note
            );

            seekBar.setProgress(0);

            currentTime.setText("0:00");

            totalTime.setText("0:00");

            clearLyrics();

            return;
        }

        if (track.getId() != null &&
                track.getId().equals(displayedTrackId)) {

            return;
        }

        displayedTrackId =
                track.getId();

        title.setText(
                track.getTitle()
        );

        artist.setText(
                track.getArtist()
        );

        loadAlbumArtwork(track);

        updateProgress();
    }

    // ---------------------------------------------------------
    // Album artwork
    // ---------------------------------------------------------

    private void loadAlbumArtwork(
            MusicTrack track
    ) {

        if (track == null ||
                track.getArtworkUri() == null) {

            albumArt.setImageResource(
                    R.drawable.ic_music_note
            );

            return;
        }

        Uri artworkUri =
                track.getArtworkUri();

        String requestedTrackId =
                track.getId();

        artworkExecutor.execute(() -> {

            Bitmap bitmap = null;

            try (InputStream inputStream =
                         getContentResolver()
                                 .openInputStream(
                                         artworkUri
                                 )) {

                if (inputStream != null) {

                    bitmap =
                            BitmapFactory.decodeStream(
                                    inputStream
                            );
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            Bitmap finalBitmap = bitmap;

            runOnUiThread(() -> {

                if (isFinishing() ||
                        isDestroyed()) {

                    return;
                }

                if (requestedTrackId != null &&
                        !requestedTrackId.equals(
                                displayedTrackId
                        )) {

                    return;
                }

                if (finalBitmap != null) {

                    albumArt.setImageBitmap(
                            finalBitmap
                    );

                } else {

                    albumArt.setImageResource(
                            R.drawable.ic_music_note
                    );
                }
            });
        });
    }

    // ---------------------------------------------------------
    // Progress
    // ---------------------------------------------------------

    private void updateProgress() {

        if (musicManager == null) {
            return;
        }

        MusicTrack currentTrack =
                musicManager.getCurrentTrack();

        // Detect a track that was already playing before
        // this activity connected to MusicManager.
        if (currentTrack != null &&
                (displayedTrackId == null ||
                        !currentTrack.getId()
                                .equals(displayedTrackId))) {

            updateTrackInfo(currentTrack);

            if (lyricsTrackId == null ||
                    !currentTrack.getId()
                            .equals(lyricsTrackId)) {

                loadLyricsForCurrentTrack();
            }
        }

        long duration =
                musicManager.getDuration();

        long position =
                musicManager.getCurrentPosition();

        if (duration < 0) {
            duration = 0;
        }

        if (position < 0) {
            position = 0;
        }

        if (!isUserSeeking) {

            seekBar.setMax(
                    (int) Math.min(
                            duration,
                            Integer.MAX_VALUE
                    )
            );

            seekBar.setProgress(
                    (int) Math.min(
                            position,
                            Integer.MAX_VALUE
                    )
            );

            currentTime.setText(
                    formatTime(position)
            );
        }

        totalTime.setText(
                formatTime(duration)
        );

        updatePlayPauseButton(
                musicManager.isPlaying()
        );

        // Keep synchronized lyrics following playback.
        updateLyrics(position);
    }

    // ---------------------------------------------------------
    // Lyrics loading
    // ---------------------------------------------------------

    private void loadLyricsForCurrentTrack() {

        if (lyricsManager == null ||
                musicManager == null) {

            return;
        }

        MusicTrack track =
                musicManager.getCurrentTrack();

        if (track == null) {

            lyricsTrackId = null;

            clearLyrics();

            return;
        }

        String trackId =
                track.getId();

        if (trackId != null &&
                trackId.equals(lyricsTrackId) &&
                currentLyrics != null) {

            return;
        }

        lyricsTrackId = trackId;

        currentLyrics = null;

        clearLyrics();

        lyricsStatus.setText(
                "Loading lyrics..."
        );

        lyricsManager.getLyrics(
                track,
                new LyricsRepository.Callback() {

                    @Override
                    public void onSuccess(
                            Lyrics lyrics
                    ) {

                        runOnUiThread(() -> {

                            // Make sure the response still belongs
                            // to the currently playing track.
                            MusicTrack currentTrack =
                                    musicManager
                                            .getCurrentTrack();

                            if (currentTrack == null) {
                                return;
                            }

                            if (track.getId() != null &&
                                    !track.getId()
                                            .equals(
                                                    currentTrack.getId()
                                            )) {

                                return;
                            }

                            currentLyrics =
                                    lyrics;

                            if (lyrics != null &&
                                    lyrics.hasSyncedLyrics()) {

                                lyricsStatus.setText(
                                        "Synced lyrics • LRCLIB"
                                );

                            } else if (lyrics != null &&
                                    lyrics.hasPlainLyrics()) {

                                lyricsStatus.setText(
                                        "Plain lyrics • LRCLIB"
                                );

                            } else {

                                lyricsStatus.setText(
                                        "Lyrics unavailable"
                                );
                            }

                            updateLyrics(
                                    musicManager
                                            .getCurrentPosition()
                            );
                        });
                    }

                    @Override
                    public void onError(
                            Exception error
                    ) {

                        runOnUiThread(() -> {

                            currentLyrics = null;

                            lyricsStatus.setText(
                                    "Lyrics unavailable"
                            );

                            clearLyricText();
                        });
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // Lyrics update
    // ---------------------------------------------------------

    private void updateLyrics(
            long positionMs
    ) {

        if (currentLyrics == null) {
            return;
        }

        // -----------------------------------------------------
        // Synced lyrics
        // -----------------------------------------------------

        if (currentLyrics.hasSyncedLyrics()) {

            int index =
                    lyricsManager.getCurrentLineIndex(
                            currentLyrics,
                            positionMs
                    );

            if (index < 0) {

                clearLyricText();

                if (!currentLyrics
                        .getSyncedLines()
                        .isEmpty()) {

                    nextLyric.setText(
                            currentLyrics
                                    .getSyncedLines()
                                    .get(0)
                                    .getText()
                    );
                }

                return;
            }

            // Previous line
            if (index > 0) {

                LyricsLine previousLine =
                        currentLyrics
                                .getSyncedLines()
                                .get(index - 1);

                previousLyric.setText(
                        previousLine.getText()
                );

            } else {

                previousLyric.setText("");
            }

            // Current line
            LyricsLine currentLine =
                    currentLyrics
                            .getSyncedLines()
                            .get(index);

            currentLyric.setText(
                    currentLine.getText()
            );

            // Next line
            if (index + 1 <
                    currentLyrics
                            .getSyncedLines()
                            .size()) {

                LyricsLine nextLine =
                        currentLyrics
                                .getSyncedLines()
                                .get(index + 1);

                nextLyric.setText(
                        nextLine.getText()
                );

            } else {

                nextLyric.setText("");
            }

            return;
        }

        // -----------------------------------------------------
        // Plain lyrics
        // -----------------------------------------------------

        if (currentLyrics.hasPlainLyrics()) {

            previousLyric.setText("");

            currentLyric.setText(
                    currentLyrics.getPlainLyrics()
            );

            nextLyric.setText("");

            return;
        }

        clearLyricText();
    }

    // ---------------------------------------------------------
    // Clear lyrics
    // ---------------------------------------------------------

    private void clearLyrics() {

        if (lyricsStatus != null) {

            lyricsStatus.setText(
                    "Lyrics not loaded"
            );
        }

        clearLyricText();
    }

    private void clearLyricText() {

        if (previousLyric != null) {
            previousLyric.setText("");
        }

        if (currentLyric != null) {
            currentLyric.setText("");
        }

        if (nextLyric != null) {
            nextLyric.setText("");
        }
    }

    // ---------------------------------------------------------
    // Play / pause
    // ---------------------------------------------------------

    private void updatePlayPauseButton(
            boolean isPlaying
    ) {

        playPause.setImageResource(
                isPlaying
                        ? R.drawable.pause
                        : R.drawable.play
        );
    }

    // ---------------------------------------------------------
    // Time formatting
    // ---------------------------------------------------------

    private String formatTime(
            long milliseconds
    ) {

        long totalSeconds =
                milliseconds / 1000;

        long minutes =
                totalSeconds / 60;

        long seconds =
                totalSeconds % 60;

        return String.format(
                Locale.getDefault(),
                "%d:%02d",
                minutes,
                seconds
        );
    }

    // ---------------------------------------------------------
    // Destroy
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(
                progressUpdater
        );

        artworkExecutor.shutdownNow();

        super.onDestroy();
    }
}


