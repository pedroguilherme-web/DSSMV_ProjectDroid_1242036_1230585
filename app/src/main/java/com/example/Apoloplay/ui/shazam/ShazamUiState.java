package com.example.Apoloplay.ui.shazam;

import com.example.Apoloplay.data.model.Music;

/**
 * Representa o estado da interface do Shazam.
 * Segue o padrão UiState comum ao resto da aplicação (DATA / LOADING / ERROR / etc.)
 */
public class ShazamUiState {

    public enum Status {
        IDLE,       // inicial
        RECORDING,  // a gravar áudio
        LOADING,    // a enviar para API
        DATA,       // música reconhecida
        ERROR       // erro na operação
    }

    private final Status status;
    private final Music music;
    private final String error;

    private ShazamUiState(Status status, Music music, String error) {
        this.status = status;
        this.music = music;
        this.error = error;
    }

    public static ShazamUiState idle()      { return new ShazamUiState(Status.IDLE, null, null); }
    public static ShazamUiState recording() { return new ShazamUiState(Status.RECORDING, null, null); }
    public static ShazamUiState loading()   { return new ShazamUiState(Status.LOADING, null, null); }
    public static ShazamUiState data(Music m) { return new ShazamUiState(Status.DATA, m, null); }
    public static ShazamUiState error(String e) { return new ShazamUiState(Status.ERROR, null, e); }

    // --- Getters ---
    public Status getStatus() { return status; }
    public Music getMusic() { return music; }
    public String getError() { return error; }
}
