package com.example.softbackgrounds.music;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicPlaylistRepository {

    private static final String PREFS =
            "music_playlists";

    private static final String PLAYLISTS =
            "playlists";

    private final SharedPreferences preferences;

    public MusicPlaylistRepository(Context context) {

        preferences = context.getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public List<MusicPlaylist> getAllPlaylists() {

        List<MusicPlaylist> result =
                new ArrayList<>();

        String json = preferences.getString(
                PLAYLISTS,
                "[]"
        );

        try {

            JSONArray playlists =
                    new JSONArray(json);

            for (int i = 0;
                 i < playlists.length();
                 i++) {

                JSONObject object =
                        playlists.getJSONObject(i);

                String id =
                        object.getString("id");

                String name =
                        object.getString("name");

                JSONArray tracks =
                        object.optJSONArray("tracks");

                List<String> trackIds =
                        new ArrayList<>();

                if (tracks != null) {

                    for (int j = 0;
                         j < tracks.length();
                         j++) {

                        trackIds.add(
                                tracks.getString(j)
                        );
                    }
                }

                result.add(
                        new MusicPlaylist(
                                id,
                                name,
                                trackIds
                        )
                );
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    public MusicPlaylist createPlaylist(
            String name
    ) {

        String id =
                String.valueOf(
                        System.currentTimeMillis()
                );

        MusicPlaylist playlist =
                new MusicPlaylist(id, name);

        List<MusicPlaylist> playlists =
                getAllPlaylists();

        playlists.add(playlist);

        save(playlists);

        return playlist;
    }

    public void deletePlaylist(String playlistId) {

        List<MusicPlaylist> playlists =
                getAllPlaylists();

        playlists.removeIf(
                playlist ->
                        playlist.getId()
                                .equals(playlistId)
        );

        save(playlists);
    }

    public void renamePlaylist(
            String playlistId,
            String newName
    ) {

        List<MusicPlaylist> playlists =
                getAllPlaylists();

        for (MusicPlaylist playlist : playlists) {

            if (playlist.getId()
                    .equals(playlistId)) {

                playlist.setName(newName);
                break;
            }
        }

        save(playlists);
    }

    public void addTrack(
            String playlistId,
            String trackId
    ) {

        List<MusicPlaylist> playlists =
                getAllPlaylists();

        for (MusicPlaylist playlist : playlists) {

            if (playlist.getId()
                    .equals(playlistId)) {

                playlist.addTrack(trackId);
                break;
            }
        }

        save(playlists);
    }

    public void removeTrack(
            String playlistId,
            String trackId
    ) {

        List<MusicPlaylist> playlists =
                getAllPlaylists();

        for (MusicPlaylist playlist : playlists) {

            if (playlist.getId()
                    .equals(playlistId)) {

                playlist.removeTrack(trackId);
                break;
            }
        }

        save(playlists);
    }

    private void save(
            List<MusicPlaylist> playlists
    ) {

        JSONArray array =
                new JSONArray();

        try {

            for (MusicPlaylist playlist : playlists) {

                JSONObject object =
                        new JSONObject();

                object.put(
                        "id",
                        playlist.getId()
                );

                object.put(
                        "name",
                        playlist.getName()
                );

                JSONArray tracks =
                        new JSONArray();

                for (String trackId :
                        playlist.getTrackIds()) {

                    tracks.put(trackId);
                }

                object.put(
                        "tracks",
                        tracks
                );

                array.put(object);
            }

        } catch (Exception ignored) {
        }

        preferences.edit()
                .putString(
                        PLAYLISTS,
                        array.toString()
                )
                .apply();
    }
}
