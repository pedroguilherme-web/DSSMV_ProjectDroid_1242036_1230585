package com.example.Apoloplay.ui.playlistdetails;

import androidx.annotation.Nullable;

import com.example.Apoloplay.domain.model.Music;

import java.util.Collections;
import java.util.List;

/** Estado único para o ecrã de detalhes da playlist (loading / data / error). */
public final class PlaylistDetailsUiState {

    public enum Status { LOADING, DATA, ERROR }

    private final Status status;
    private final List<Music> tracks;
    private final String errorMessage;

    private PlaylistDetailsUiState(Status status, @Nullable List<Music> tracks, @Nullable String errorMessage) {
        this.status = status;
        this.tracks = tracks == null ? Collections.emptyList() : Collections.unmodifiableList(tracks);
        this.errorMessage = errorMessage;
    }

    // Fábricas
    public static PlaylistDetailsUiState loading() {
        return new PlaylistDetailsUiState(Status.LOADING, null, null);
    }

    public static PlaylistDetailsUiState data(List<Music> tracks) {
        return new PlaylistDetailsUiState(Status.DATA, tracks, null);
    }

    public static PlaylistDetailsUiState error(String message) {
        return new PlaylistDetailsUiState(Status.ERROR, null, message);
    }

    // Getters
    public Status getStatus() { return status; }
    public List<Music> getTracks() { return tracks; }
    public String getErrorMessage() { return errorMessage; }
}
