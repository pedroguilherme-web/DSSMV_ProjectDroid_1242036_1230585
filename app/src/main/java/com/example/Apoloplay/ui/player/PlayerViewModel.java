package com.example.Apoloplay.ui.player;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.domain.model.Music;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class PlayerViewModel extends ViewModel {

    // Mantém igual aos que já usas na app (ou aponta para um Config central)
    private static final String CLIENT_ID    = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    private final MutableLiveData<PlayerUiState> state = new MutableLiveData<>(PlayerUiState.idle());
    private SpotifyAppRemote appRemote;

    // Se pedirem play antes de conectar
    private String pendingPlayUri;

    public LiveData<PlayerUiState> getState() {
        return state;
    }

    // -------- Conexão --------

    public void connect(Context ctx) {
        if (appRemote != null) {
            // já está “ligado” (ou a tentar)
            return;
        }

        ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(ctx, params, new Connector.ConnectionListener() {
            @Override public void onConnected(SpotifyAppRemote remote) {
                appRemote = remote;
                post(s().withConnected(true));

                // se havia um play pendente, dispara agora
                if (pendingPlayUri != null) {
                    internalPlay(pendingPlayUri);
                    pendingPlayUri = null;
                }
            }

            @Override public void onFailure(Throwable error) {
                Log.e("PlayerVM", "App Remote connect failed: " + error.getMessage(), error);
                appRemote = null;
                post(PlayerUiState.idle().withError("Falha a ligar ao Spotify App Remote"));
            }
        });
    }

    public void disconnect() {
        if (appRemote != null) {
            try { SpotifyAppRemote.disconnect(appRemote); } catch (Throwable ignore) {}
            appRemote = null;
        }
        post(PlayerUiState.idle());
    }

    // -------- Comandos de reprodução --------

    /** Play explícito (recomendado): toca SEM alternar. */
    public void play(Context ctx, Music music) {
        String uri = (music != null) ? music.getSpotifyTrackUri() : null;
        if (uri == null || uri.isEmpty()) {
            post(s().withError("Sem URI Spotify para esta faixa."));
            return;
        }
        if (appRemote == null) {
            pendingPlayUri = uri;
            connect(ctx);
            return;
        }
        internalPlay(uri);
    }


    public void pause() {
        if (appRemote == null) {
            post(s().withError("Não está ligado ao Spotify."));
            return;
        }
        appRemote.getPlayerApi().pause();
        post(s().withPlaying(false, s().currentTrackUri));
    }


    public void toggle(Context ctx, Music music) {
        PlayerUiState cur = s();
        if (cur.isPlaying) pause();
        else play(ctx, music);
    }

    // -------- Internos --------

    private void internalPlay(String uri) {
        try {
            appRemote.getPlayerApi().play(uri);
            post(s().withPlaying(true, uri));
        } catch (Throwable t) {
            Log.e("PlayerVM", "play failed", t);
            post(s().withError("Não foi possível tocar a faixa."));
        }
    }

    private PlayerUiState s() {
        PlayerUiState cur = state.getValue();
        return (cur != null) ? cur : PlayerUiState.idle();
    }

    private void post(PlayerUiState ns) {
        state.postValue(ns);
    }

    @Override protected void onCleared() {
        super.onCleared();
        disconnect();
    }
}
