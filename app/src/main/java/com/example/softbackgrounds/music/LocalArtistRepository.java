package com.example.softbackgrounds.music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class LocalArtistRepository {

    private final ContentResolver resolver;

    public LocalArtistRepository(Context context) {
        resolver = context.getApplicationContext()
                .getContentResolver();
    }

    public List<MusicArtist> getAllArtists() {

        List<MusicArtist> artists = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
        };

        String sortOrder =
                MediaStore.Audio.Artists.ARTIST +
                        " COLLATE NOCASE ASC";

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {

            if (cursor == null) {
                return artists;
            }

            int idColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Artists._ID);

            int nameColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Artists.ARTIST);

            int tracksColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

            int albumsColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);

                String name =
                        cursor.getString(nameColumn);

                int trackCount =
                        cursor.getInt(tracksColumn);

                int albumCount =
                        cursor.getInt(albumsColumn);

                if (name == null ||
                        name.trim().isEmpty() ||
                        name.equals("<unknown>")) {

                    name = "Unknown artist";
                }

                artists.add(new MusicArtist(
                        id,
                        name.trim(),
                        trackCount,
                        albumCount
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return artists;
    }
}
