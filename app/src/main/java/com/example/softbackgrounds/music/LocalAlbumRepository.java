package com.example.softbackgrounds.music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class LocalAlbumRepository {

    private final ContentResolver resolver;

    public LocalAlbumRepository(Context context) {
        resolver = context.getApplicationContext()
                .getContentResolver();
    }

    public List<MusicAlbum> getAllAlbums() {

        List<MusicAlbum> albums = new ArrayList<>();

        Uri collection =
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };

        String sortOrder =
                MediaStore.Audio.Albums.ALBUM +
                        " COLLATE NOCASE ASC";

        try (Cursor cursor = resolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
        )) {

            if (cursor == null) {
                return albums;
            }

            int idColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Albums._ID);

            int albumColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Albums.ALBUM);

            int artistColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Albums.ARTIST);

            int countColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);

                String name = cursor.getString(albumColumn);
                String artist = cursor.getString(artistColumn);

                int trackCount =
                        cursor.getInt(countColumn);

                Uri artworkUri = Uri.parse(
                        "content://media/external/audio/albumart/"
                                + id
                );

                albums.add(new MusicAlbum(
                        id,
                        clean(name, "Unknown album"),
                        clean(artist, "Unknown artist"),
                        artworkUri,
                        trackCount
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return albums;
    }

    private String clean(String value, String fallback) {

        if (value == null ||
                value.trim().isEmpty() ||
                value.equals("<unknown>")) {

            return fallback;
        }

        return value.trim();
    }
}
