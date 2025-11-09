package com.example.Apoloplay.ui.player;

import androidx.annotation.Nullable;

public class PlayerUiState {
    public final boolean connected;
    public final boolean isPlaying;
    @Nullable public final String currentTrackUri;
    @Nullable public final String error;

    public PlayerUiState(boolean connected, boolean isPlaying,
                         @Nullable String currentTrackUri, @Nullable String error) {
        this.connected = connected;
        this.isPlaying = isPlaying;
        this.currentTrackUri = currentTrackUri;
        this.error = error;
    }

    public static PlayerUiState idle() {
        return new PlayerUiState(false, false, null, null);
    }

    public PlayerUiState withError(String e) {
        return new PlayerUiState(this.connected, this.isPlaying, this.currentTrackUri, e);
    }

    public PlayerUiState withConnected(boolean c) {
        return new PlayerUiState(c, this.isPlaying, this.currentTrackUri, null);
    }

    public PlayerUiState withPlaying(boolean p, @Nullable String uri) {
        return new PlayerUiState(this.connected, p, uri, null);
    }
}
