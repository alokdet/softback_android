
package com.example.softbackgrounds.music;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LocalMusicRepository {

    private final ContentResolver resolver;

    public LocalMusicRepository(Context context) {
        resolver = context.getApplicationContext()
                .getContentResolver();
    }

    public List<MusicTrack> getAllTracks() {
        return getAllTracks(MusicSortOrder.TITLE_ASC);
    }

    public List<MusicTrack> getAllTracks(MusicSortOrder order) {
        return queryTracks(null, null, getSortOrder(order), 0);
    }

    public List<MusicTrack> searchTracks(String query) {

        if (query == null || query.trim().isEmpty()) {
            return getAllTracks();
        }

        String search = "%" + escapeLike(query.trim()) + "%";

        String selection =
                MediaStore.Audio.Media.IS_MUSIC + " != 0 AND (" +
                        MediaStore.Audio.Media.TITLE + " LIKE ? ESCAPE '\\' OR " +
                        MediaStore.Audio.Media.ARTIST + " LIKE ? ESCAPE '\\' OR " +
                        MediaStore.Audio.Media.ALBUM + " LIKE ? ESCAPE '\\')";

        String[] selectionArgs = {
                search, search, search
        };

        return queryTracks(
                selection,
                selectionArgs,
                getSortOrder(MusicSortOrder.TITLE_ASC),
                0
        );
    }

    public List<MusicTrack> getRecentlyAdded(int limit) {

        if (limit <= 0) return Collections.emptyList();

        List<MusicTrack> tracks = queryTracks(
                null,
                null,
                getSortOrder(MusicSortOrder.DATE_ADDED_DESC),
                limit
        );

        return tracks;
    }

    private List<MusicTrack> queryTracks(
            String selection,
            String[] selectionArgs,
            String sortOrder,
            int limit
    ) {

        List<MusicTrack> tracks = new ArrayList<>();

        Uri collection =
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String baseSelection =
                MediaStore.Audio.Media.IS_MUSIC + " != 0";

        if (selection != null && !selection.isEmpty()) {
            selection = baseSelection + " AND (" + selection + ")";
        } else {
            selection = baseSelection;
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };

        try (Cursor cursor = resolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {

            if (cursor == null) return tracks;

            int idColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media._ID);

            int titleColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.TITLE);

            int artistColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ARTIST);

            int albumColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ALBUM);

            int albumIdColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.ALBUM_ID);

            int durationColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                long albumId = cursor.getLong(albumIdColumn);

                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String album = cursor.getString(albumColumn);

                long duration = cursor.getLong(durationColumn);

                Uri trackUri = Uri.withAppendedPath(
                        collection,
                        String.valueOf(id)
                );

                Uri artworkUri = albumId > 0
                        ? Uri.parse(
                        "content://media/external/audio/albumart/"
                                + albumId
                )
                        : null;

                tracks.add(new MusicTrack(
                        String.valueOf(id),
                        cleanMetadata(title, "Unknown title"),
                        cleanMetadata(artist, "Unknown artist"),
                        cleanMetadata(album, "Unknown album"),
                        trackUri,
                        artworkUri,
                        duration
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tracks;
    }

    public List<MusicTrack> getTracksForAlbum(long albumId) {

        String selection =
                MediaStore.Audio.Media.ALBUM_ID + " = ?";

        String[] args = {
                String.valueOf(albumId)
        };

        return queryTracks(
                selection,
                args,
                MediaStore.Audio.Media.TRACK + " ASC",
                0
        );
    }

    public List<MusicTrack> getTracksForArtist(long artistId) {

        String selection =
                MediaStore.Audio.Media.ARTIST_ID + " = ?";

        String[] args = {
                String.valueOf(artistId)
        };

        return queryTracks(
                selection,
                args,
                MediaStore.Audio.Media.ALBUM +
                        " COLLATE NOCASE ASC, " +
                        MediaStore.Audio.Media.TRACK +
                        " ASC",
                0
        );
    }

    private String getSortOrder(MusicSortOrder order) {

        if (order == null) {
            order = MusicSortOrder.TITLE_ASC;
        }

        switch (order) {

            case TITLE_DESC:
                return MediaStore.Audio.Media.TITLE
                        + " COLLATE NOCASE DESC";

            case ARTIST_ASC:
                return MediaStore.Audio.Media.ARTIST
                        + " COLLATE NOCASE ASC";

            case ALBUM_ASC:
                return MediaStore.Audio.Media.ALBUM
                        + " COLLATE NOCASE ASC";

            case DATE_ADDED_DESC:
                return MediaStore.Audio.Media.DATE_ADDED + " DESC";

            case DURATION_DESC:
                return MediaStore.Audio.Media.DURATION + " DESC";

            case TITLE_ASC:
            default:
                return MediaStore.Audio.Media.TITLE
                        + " COLLATE NOCASE ASC";
        }
    }

    private String cleanMetadata(String value, String fallback) {

        if (value == null || value.trim().isEmpty()
                || value.equals("<unknown>")) {
            return fallback;
        }

        return value.trim();
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}