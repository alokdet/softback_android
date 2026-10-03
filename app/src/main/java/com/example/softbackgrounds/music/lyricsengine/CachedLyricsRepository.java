package com.example.softbackgrounds.music.lyricsengine;

import android.content.Context;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CachedLyricsRepository {

    private static final String CACHE_DIR = "lyrics_cache";

    private final File directory;

    public CachedLyricsRepository(Context context) {
        directory = new File(
                context.getCacheDir(),
                CACHE_DIR
        );

        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public Lyrics get(String trackId) {

        if (trackId == null || trackId.isEmpty()) {
            return null;
        }

        File file = getFile(trackId);

        if (!file.exists()) {
            return null;
        }

        try {

            String json = readFile(file);

            JSONObject object =
                    new JSONObject(json);

            String plainLyrics =
                    object.optString(
                            "plainLyrics",
                            ""
                    );

            String source =
                    object.optString(
                            "source",
                            "cache"
                    );

            JSONArray linesArray =
                    object.optJSONArray(
                            "syncedLines"
                    );

            List<LyricsLine> lines =
                    new ArrayList<>();

            if (linesArray != null) {

                for (int i = 0;
                     i < linesArray.length();
                     i++) {

                    JSONObject line =
                            linesArray.getJSONObject(i);

                    lines.add(
                            new LyricsLine(
                                    line.getLong(
                                            "time"
                                    ),
                                    line.optString(
                                            "text",
                                            ""
                                    )
                            )
                    );
                }
            }

            return new Lyrics(
                    plainLyrics,
                    lines,
                    source
            );

        } catch (Exception e) {

            // Corrupted cache should never
            // break the music system.
            file.delete();

            return null;
        }
    }

    public void put(
            String trackId,
            Lyrics lyrics
    ) {

        if (trackId == null ||
                trackId.isEmpty() ||
                lyrics == null) {
            return;
        }

        try {

            JSONObject object =
                    new JSONObject();

            object.put(
                    "plainLyrics",
                    lyrics.getPlainLyrics()
            );

            object.put(
                    "source",
                    lyrics.getSource()
            );

            JSONArray lines =
                    new JSONArray();

            for (LyricsLine line :
                    lyrics.getSyncedLines()) {

                JSONObject item =
                        new JSONObject();

                item.put(
                        "time",
                        line.getStartTimeMs()
                );

                item.put(
                        "text",
                        line.getText()
                );

                lines.put(item);
            }

            object.put(
                    "syncedLines",
                    lines
            );

            writeFile(
                    getFile(trackId),
                    object.toString()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String trackId) {

        File file = getFile(trackId);

        if (file.exists()) {
            file.delete();
        }
    }

    public void clear() {

        File[] files =
                directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            file.delete();
        }
    }

    private File getFile(String trackId) {

        String encoded =
                Base64.encodeToString(
                        trackId.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        Base64.URL_SAFE |
                                Base64.NO_WRAP |
                                Base64.NO_PADDING
                );

        return new File(
                directory,
                encoded + ".json"
        );
    }

    private String readFile(File file)
            throws Exception {

        FileInputStream input =
                new FileInputStream(file);

        try {

            byte[] data =
                    new byte[(int) file.length()];

            int offset = 0;
            int count;

            while (
                    offset < data.length &&
                            (count = input.read(
                                    data,
                                    offset,
                                    data.length - offset
                            )) != -1
            ) {
                offset += count;
            }

            return new String(
                    data,
                    StandardCharsets.UTF_8
            );

        } finally {
            input.close();
        }
    }

    private void writeFile(
            File file,
            String content
    ) throws Exception {

        FileOutputStream output =
                new FileOutputStream(file);

        try {

            output.write(
                    content.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            output.flush();

        } finally {
            output.close();
        }
    }
}
