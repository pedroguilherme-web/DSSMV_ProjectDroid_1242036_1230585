package com.example.Apoloplay.ui.addtoplaylist;

import androidx.annotation.Nullable;

import com.example.Apoloplay.domain.model.Playlist;

import java.util.Collections;
import java.util.List;

public final class AddToPlaylistUiState {

    public enum Status {
        IDLE,
        LOADING,
        LIST,              // temos lista de playlists
        ERROR,             // ocorreu erro
        ADDED,             // faixa adicionada com sucesso
        PLAYLIST_CREATED   // playlist criada com sucesso
    }

    private final Status status;
    private final List<Playlist> playlists;
    @Nullable private final String message; // para erros / toasts

    private AddToPlaylistUiState(Status status,
                                 @Nullable List<Playlist> playlists,
                                 @Nullable String message) {
        this.status = status;
        this.playlists = playlists == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(playlists);
        this.message = message;
    }

    // ----- Fábricas estáticas -----

    public static AddToPlaylistUiState idle() {
        return new AddToPlaylistUiState(Status.IDLE, null, null);
    }

    public static AddToPlaylistUiState loading() {
        return new AddToPlaylistUiState(Status.LOADING, null, null);
    }

    public static AddToPlaylistUiState list(List<Playlist> playlists) {
        return new AddToPlaylistUiState(Status.LIST, playlists, null);
    }

    public static AddToPlaylistUiState error(String message) {
        return new AddToPlaylistUiState(Status.ERROR, null, message);
    }

    public static AddToPlaylistUiState added() {
        return new AddToPlaylistUiState(Status.ADDED, null, null);
    }

    public static AddToPlaylistUiState playlistCreated() {
        return new AddToPlaylistUiState(Status.PLAYLIST_CREATED, null, null);
    }

    // ----- Getters -----

    public Status getStatus() { return status; }

    public List<Playlist> getPlaylists() { return playlists; }

    @Nullable
    public String getMessage() { return message; }
}
