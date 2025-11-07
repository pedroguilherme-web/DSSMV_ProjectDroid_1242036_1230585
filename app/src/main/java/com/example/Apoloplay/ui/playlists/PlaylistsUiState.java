package com.example.Apoloplay.ui.playlists;

import com.example.Apoloplay.domain.model.Playlist;
import java.util.Collections;
import java.util.List;

public class PlaylistsUiState {
    public final boolean loading;
    public final List<Playlist> items;
    public final String error;

    public PlaylistsUiState(boolean loading, List<Playlist> items, String error) {
        this.loading = loading;
        this.items = items != null ? items : Collections.emptyList();
        this.error = error;
    }

    public static PlaylistsUiState idle(){ return new PlaylistsUiState(false, Collections.emptyList(), null); }
    public static PlaylistsUiState loading(){ return new PlaylistsUiState(true, Collections.emptyList(), null); }
    public static PlaylistsUiState success(List<Playlist> data){ return new PlaylistsUiState(false, data, null); }
    public static PlaylistsUiState error(String msg){ return new PlaylistsUiState(false, Collections.emptyList(), msg); }
}
