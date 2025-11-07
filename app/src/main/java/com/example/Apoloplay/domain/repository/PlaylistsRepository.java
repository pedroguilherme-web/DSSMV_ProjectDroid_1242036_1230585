package com.example.Apoloplay.domain.repository;

import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.model.Playlist;

import java.util.List;

public interface PlaylistsRepository {

    interface PlaylistsCallback {
        void onSuccess(List<Playlist> data);
        void onError(String message);
    }

    interface TracksCallback {
        void onSuccess(List<Music> data);
        void onError(String message);
    }

    interface PlaylistCallback {
        void onSuccess(Playlist playlist);
        void onError(String message);
    }

    interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    void getMyPlaylists(int limit, int offset, PlaylistsCallback cb);

    void getPlaylistTracks(String playlistId, int limit, int offset, TracksCallback cb);

    void createPlaylist(String name, String description, boolean isPublic, PlaylistCallback cb);

    void addTrackToPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb);

    void removeTrackFromPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb);

    void deletePlaylist(String playlistId, SimpleCallback cb);
}

