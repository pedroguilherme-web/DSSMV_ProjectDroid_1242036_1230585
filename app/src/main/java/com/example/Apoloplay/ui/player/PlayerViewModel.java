package com.example.Apoloplay.ui.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.domain.model.Music;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;

import java.io.IOException;

public class PlayerViewModel extends ViewModel {

    private static final String TAG = "PlayerVM";


    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    private final MutableLiveData<PlayerUiState> state = new MutableLiveData<>(PlayerUiState.idle());
    public LiveData<PlayerUiState> getState() { return state; }

    private SpotifyAppRemote appRemote;
    private Subscription<PlayerState> playerSub;
    private MediaPlayer mediaPlayer;

    private boolean loopOne = false;
    private String requestedUri = null;
    private boolean commandInFlight = false;
    private long suppressUntilMs = 0L;

    // --- Connection ---
    public void connect(Context ctx) {
        if (appRemote != null && appRemote.isConnected()) {
            state.postValue(PlayerUiState.connected(false, loopOne, requestedUri));
            return;
        }
        ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(ctx, params, new Connector.ConnectionListener() {
            @Override public void onConnected(SpotifyAppRemote remote) {
                appRemote = remote;
                state.postValue(PlayerUiState.connected(false, loopOne, null));
                subscribePlayerState();
            }
            @Override public void onFailure(Throwable error) {
                Log.e(TAG, "AppRemote connect fail", error);
                state.postValue(PlayerUiState.error("Falha a ligar ao Spotify App Remote"));
            }
        });
    }

    public void disconnect() {
        if (playerSub != null && !playerSub.isCanceled()) {
            try { playerSub.cancel(); } catch (Throwable ignore) {}
            playerSub = null;
        }
        releasePreview();
        if (appRemote != null) {
            try { SpotifyAppRemote.disconnect(appRemote); } catch (Throwable ignore) {}
            appRemote = null;
        }
        state.postValue(PlayerUiState.idle());
    }

    private void subscribePlayerState() {
        if (appRemote == null || !appRemote.isConnected()) return;
        if (playerSub != null && !playerSub.isCanceled()) playerSub.cancel();
        playerSub = appRemote.getPlayerApi().subscribeToPlayerState();
        playerSub.setEventCallback(ps -> {
            long now = System.currentTimeMillis();
            if (now < suppressUntilMs) {
                state.postValue(PlayerUiState.connected(!ps.isPaused, loopOne,
                        ps.track != null ? ps.track.uri : null));
                return;
            }

            boolean paused = ps.isPaused;
            String currentUri = (ps.track != null) ? ps.track.uri : null;

            // se pedimos algo e mudou, “normalizar”
            if (!loopOne && requestedUri != null && currentUri != null
                    && !requestedUri.equals(currentUri) && !commandInFlight) {
                try { appRemote.getPlayerApi().pause(); } catch (Throwable ignore) {}
                try { appRemote.getPlayerApi().seekTo(0); } catch (Throwable ignore) {}
                suppressUntilMs = System.currentTimeMillis() + 600;
                state.postValue(PlayerUiState.connected(false, loopOne, currentUri));
                return;
            }

            if (requestedUri != null && requestedUri.equals(currentUri) && commandInFlight) {
                commandInFlight = false;
            }

            state.postValue(PlayerUiState.connected(!paused, loopOne, currentUri));
        });
    }

    // --- Playback public API ---
    public void playOrToggle(Context ctx, Music music) {
        if (music == null) return;

        if (appRemote != null && appRemote.isConnected()) {
            appRemote.getPlayerApi().getPlayerState().setResultCallback(ps -> {
                String currentUri = (ps.track != null) ? ps.track.uri : null;
                boolean same = currentUri != null && currentUri.equals(music.getSpotifyTrackUri());

                if (same) {
                    if (ps.isPaused) {
                        appRemote.getPlayerApi().resume();
                        state.postValue(PlayerUiState.connected(true, loopOne, currentUri));
                    } else {
                        pause();
                    }
                    return;
                }

                try {
                    appRemote.getPlayerApi().setShuffle(false);
                    appRemote.getPlayerApi().setRepeat(loopOne ? Repeat.ONE : Repeat.OFF);
                } catch (Throwable ignore) {}

                try { appRemote.getPlayerApi().pause(); } catch (Throwable ignore) {}
                suppressUntilMs = System.currentTimeMillis() + 600;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    requestedUri = music.getSpotifyTrackUri();
                    commandInFlight = true;
                    appRemote.getPlayerApi().play(requestedUri);
                    try { appRemote.getPlayerApi().seekTo(0); } catch (Throwable ignore) {}
                    state.postValue(PlayerUiState.connected(true, loopOne, requestedUri));
                }, 250);
            });
            return;
        }

        // fallback: preview 30s
        if (music.getPreviewUrl() != null && !music.getPreviewUrl().isEmpty()) {
            togglePreview(music.getPreviewUrl());
        } else {
            state.postValue(PlayerUiState.error("Sem reprodução disponível (faixa sem preview)."));
        }
    }

    public void pause() {
        try { if (appRemote != null && appRemote.isConnected()) appRemote.getPlayerApi().pause(); }
        catch (Throwable ignore) {}
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        suppressUntilMs = System.currentTimeMillis() + 250;
        state.postValue(PlayerUiState.connected(false, loopOne, requestedUri));
    }

    public void setRepeatOne(boolean enabled) {
        loopOne = enabled;
        try {
            if (appRemote != null && appRemote.isConnected()) {
                appRemote.getPlayerApi().setRepeat(enabled ? Repeat.ONE : Repeat.OFF);
            }
        } catch (Throwable ignore) {}
        PlayerUiState cur = state.getValue();
        state.postValue(cur == null
                ? PlayerUiState.connected(false, loopOne, requestedUri)
                : new PlayerUiState(cur.connected, cur.isPlaying, loopOne, cur.error, cur.currentUri));
    }

    // --- Preview helpers ---
    private void togglePreview(String url) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(url);
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    state.postValue(PlayerUiState.connected(true, loopOne, requestedUri));
                });
                mediaPlayer.setOnCompletionListener(mp -> releasePreview());
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    releasePreview();
                    state.postValue(PlayerUiState.error("Erro no preview"));
                    return true;
                });
                mediaPlayer.prepareAsync();
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    state.postValue(PlayerUiState.connected(false, loopOne, requestedUri));
                } else {
                    mediaPlayer.start();
                    state.postValue(PlayerUiState.connected(true, loopOne, requestedUri));
                }
            }
        } catch (IOException e) {
            releasePreview();
            state.postValue(PlayerUiState.error("Erro a carregar preview"));
        }
    }

    private void releasePreview() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Throwable ignore) {}
            try { mediaPlayer.release(); } catch (Throwable ignore) {}
            mediaPlayer = null;
        }
    }

    @Override protected void onCleared() {
        disconnect();
        super.onCleared();
    }
}
