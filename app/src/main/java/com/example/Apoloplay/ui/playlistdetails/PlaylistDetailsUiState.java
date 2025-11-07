package com.example.Apoloplay.ui.playlistdetails;

import com.example.Apoloplay.domain.model.Music;
import java.util.Collections;
import java.util.List;

public class PlaylistDetailsUiState {
    public final boolean loading;
    public final List<Music> tracks;
    public final String error;

    public PlaylistDetailsUiState(boolean loading, List<Music> tracks, String error) {
        this.loading = loading;
        this.tracks = tracks != null ? tracks : Collections.emptyList();
        this.error = error;
    }

    public static PlaylistDetailsUiState idle(){ return new PlaylistDetailsUiState(false, Collections.emptyList(), null); }
    public static PlaylistDetailsUiState loading(){ return new PlaylistDetailsUiState(true, Collections.emptyList(), null); }
    public static PlaylistDetailsUiState success(List<Music> data){ return new PlaylistDetailsUiState(false, data, null); }
    public static PlaylistDetailsUiState error(String msg){ return new PlaylistDetailsUiState(false, Collections.emptyList(), msg); }
}
