package com.example.Apoloplay.ui.playlists;

import androidx.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import com.example.Apoloplay.domain.model.Playlist;

public final class PlaylistsUiState {
    public enum Status { LOADING, DATA, ERROR }
    private final Status status;
    private final List<Playlist> data;
    private final String errorMessage;

    private PlaylistsUiState(Status s, @Nullable List<Playlist> d, @Nullable String e) {
        this.status = s; this.data = d==null?null: Collections.unmodifiableList(d); this.errorMessage = e;
    }
    public static PlaylistsUiState loading() { return new PlaylistsUiState(Status.LOADING, null, null); }
    public static PlaylistsUiState data(List<Playlist> d) { return new PlaylistsUiState(Status.DATA, d, null); }
    public static PlaylistsUiState error(String e) { return new PlaylistsUiState(Status.ERROR, null, e); }

    public Status getStatus() { return status; }
    public List<Playlist> getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
}
