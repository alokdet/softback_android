package com.example.softbackgrounds.music.lyricsengine;

import android.os.Handler;
import android.os.Looper;

import com.example.softbackgrounds.music.MusicTrack;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LrcLibLyricsRepository implements LyricsRepository {

    private static final String BASE_URL =
            "https://lrclib.net/api/get";

    private final ExecutorService executor =
            Executors.newCachedThreadPool();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    @Override
    public void getLyrics(
            MusicTrack track,
            Callback callback
    ) {

        if (track == null) {
            callback.onError(
                    new IllegalArgumentException(
                            "Track cannot be null"
                    )
            );
            return;
        }

        executor.execute(() -> {

            try {

                String urlString = buildUrl(track);

                HttpURLConnection connection =
                        (HttpURLConnection)
                                new URL(urlString).openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(10_000);
                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );
                connection.setRequestProperty(
                        "User-Agent",
                        "SoftLauncher/1.0"
                );

                int responseCode =
                        connection.getResponseCode();

                if (responseCode == 404) {
                    throw new Exception(
                            "Lyrics not found"
                    );
                }

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    throw new Exception(
                            "LRCLIB HTTP " + responseCode
                    );
                }

                String response =
                        readResponse(
                                connection.getInputStream()
                        );

                JSONObject json =
                        new JSONObject(response);

                String plainLyrics =
                        json.optString(
                                "plainLyrics",
                                ""
                        );

                String syncedLyrics =
                        json.optString(
                                "syncedLyrics",
                                ""
                        );

                List<LyricsLine> syncedLines =
                        LrcParser.parse(
                                syncedLyrics
                        );

                Lyrics lyrics =
                        new Lyrics(
                                plainLyrics,
                                syncedLines,
                                "LRCLIB"
                        );

                mainHandler.post(
                        () -> callback.onSuccess(lyrics)
                );

                connection.disconnect();

            } catch (Exception e) {

                mainHandler.post(
                        () -> callback.onError(e)
                );
            }
        });
    }

    private String buildUrl(
            MusicTrack track
    ) throws Exception {

        StringBuilder builder =
                new StringBuilder(BASE_URL);

        builder.append("?track_name=")
                .append(
                        URLEncoder.encode(
                                safe(track.getTitle()),
                                "UTF-8"
                        )
                );

        builder.append("&artist_name=")
                .append(
                        URLEncoder.encode(
                                safe(track.getArtist()),
                                "UTF-8"
                        )
                );

        if (track.getAlbum() != null &&
                !track.getAlbum().trim().isEmpty()) {

            builder.append("&album_name=")
                    .append(
                            URLEncoder.encode(
                                    track.getAlbum(),
                                    "UTF-8"
                            )
                    );
        }

        if (track.getDuration() > 0) {

            long durationSeconds =
                    Math.round(
                            track.getDuration() / 1000.0
                    );

            builder.append("&duration=")
                    .append(durationSeconds);
        }

        return builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String readResponse(
            InputStream inputStream
    ) throws Exception {

        StringBuilder builder =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
