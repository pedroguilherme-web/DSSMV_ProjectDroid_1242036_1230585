package com.example.Apoloplay.ui.player;

import androidx.annotation.Nullable;

public class PlayerUiState {
    public final boolean connected;
    public final boolean isPlaying;
    public final boolean loopOne;
    @Nullable public final String error;
    @Nullable public final String currentUri;

    public PlayerUiState(boolean connected, boolean isPlaying, boolean loopOne,
                          @Nullable String error, @Nullable String currentUri) {
        this.connected = connected;
        this.isPlaying = isPlaying;
        this.loopOne = loopOne;
        this.error = error;
        this.currentUri = currentUri;
    }

    public static PlayerUiState idle() { return new PlayerUiState(false, false, false, null, null); }
    public static PlayerUiState connected(boolean playing, boolean loopOne, @Nullable String uri) {
        return new PlayerUiState(true, playing, loopOne, null, uri);
    }
    public static PlayerUiState error(String msg){
        return new PlayerUiState(false, false, false, msg, null);
    }
}
