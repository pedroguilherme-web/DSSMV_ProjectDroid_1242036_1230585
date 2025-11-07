package com.example.Apoloplay.ViewModel;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

/**
 * Player VM — trabalha só com domain.model.Music e Spotify App Remote.

 */
public class MusicViewModel extends ViewModel {

    // Se preferires, lê estes valores de BuildConfig.* (fields de gradle)
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> spotifyConnected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loopOneEnabled = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Boolean> getSpotifyConnectionState() { return spotifyConnected; }
    public LiveData<Boolean> getLoopOneEnabled() { return loopOneEnabled; }

    private SpotifyAppRemote spotifyAppRemote;
    private Subscription<PlayerState> playerStateSubscription;
    private MediaPlayer mediaPlayer;

    private String requestedUri = null;
    private boolean commandInFlight = false;
    private long suppressUntil = 0L;

    public void connectSpotifyAppRemote(Context context) {
        if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
            spotifyConnected.postValue(true);
            return;
        }
        ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(context, params, new Connector.ConnectionListener() {
            @Override public void onConnected(SpotifyAppRemote remote) {
                spotifyAppRemote = remote;
                spotifyConnected.postValue(true);
                requestedUri = null;
                commandInFlight = false;
                try { spotifyAppRemote.getPlayerApi().pause(); } catch (Throwable ignore) {}
                subscribeToPlayerState();
            }
            @Override public void onFailure(Throwable t) {
                Log.e("MusicVM", "Spotify App Remote connect fail: " + (t!=null?t.getMessage():"null"), t);
                spotifyConnected.postValue(false);
            }
        });
    }

    private void subscribeToPlayerState() {
        if (spotifyAppRemote == null || !spotifyAppRemote.isConnected()) return;
        if (playerStateSubscription != null && !playerStateSubscription.isCanceled()) {
            playerStateSubscription.cancel();
        }
        playerStateSubscription = spotifyAppRemote.getPlayerApi().subscribeToPlayerState();
        playerStateSubscription.setEventCallback(ps -> {
            long now = System.currentTimeMillis();
            if (now < suppressUntil) {
                isPlaying.postValue(ps != null && !ps.isPaused);
                return;
            }
            isPlaying.postValue(ps != null && !ps.isPaused);
        });
    }

    public void setRepeatOne(boolean enable) {
        loopOneEnabled.postValue(enable);
        try {
            if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) {
                spotifyAppRemote.getPlayerApi().setRepeat(enable ? Repeat.ONE : Repeat.OFF);
            }
        } catch (Throwable ignore) {}
    }

    public void playOrToggle(Context context, Music music) {
        if (music == null) return;

        final String trackUri = music.getSpotifyTrackUri();
        final String previewUrl = music.getPreviewUrl();

        if (spotifyAppRemote != null && spotifyAppRemote.isConnected() && trackUri != null) {
            spotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(ps -> {
                String currentUri = (ps.track != null) ? ps.track.uri : null;
                boolean same = currentUri != null && currentUri.equals(trackUri);

                if (same) {
                    if (ps.isPaused) {
                        spotifyAppRemote.getPlayerApi().resume();
                        isPlaying.postValue(true);
                    } else {
                        pauseTrack();
                    }
                    return;
                }

                try {
                    spotifyAppRemote.getPlayerApi().setShuffle(false);
                    spotifyAppRemote.getPlayerApi().setRepeat(Boolean.TRUE.equals(loopOneEnabled.getValue()) ? Repeat.ONE : Repeat.OFF);
                } catch (Throwable ignore) {}

                try { spotifyAppRemote.getPlayerApi().pause(); } catch (Throwable ignore) {}
                suppressUntil = System.currentTimeMillis() + 600;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    requestedUri = trackUri;
                    commandInFlight = true;
                    spotifyAppRemote.getPlayerApi().play(trackUri);
                    try { spotifyAppRemote.getPlayerApi().seekTo(0); } catch (Throwable ignore) {}
                    isPlaying.postValue(true);
                    commandInFlight = false;
                }, 250);
            });
        } else if (previewUrl != null) {
            playPreview(context, previewUrl);
        } else {
            Toast.makeText(context, "Sem reprodução disponível", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseTrack() {
        try { if (spotifyAppRemote != null && spotifyAppRemote.isConnected()) spotifyAppRemote.getPlayerApi().pause(); } catch (Throwable ignore) {}
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
        isPlaying.postValue(false);
        suppressUntil = System.currentTimeMillis() + 250;
    }

    private void playPreview(Context ctx, String url) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(url);
                mediaPlayer.setOnPreparedListener(mp -> { mp.start(); isPlaying.postValue(true); });
                mediaPlayer.setOnCompletionListener(mp -> { isPlaying.postValue(false); releaseMediaPlayer(); });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> { isPlaying.postValue(false); return false; });
                mediaPlayer.prepareAsync();
            } else {
                if (mediaPlayer.isPlaying()) { mediaPlayer.pause(); isPlaying.postValue(false); }
                else { mediaPlayer.start(); isPlaying.postValue(true); }
            }
        } catch (IOException e) {
            Toast.makeText(ctx, "Erro ao carregar prévia", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignore) {}
            try { mediaPlayer.release(); } catch (Exception ignore) {}
            mediaPlayer = null;
        }
    }

    public void releaseResources() {
        if (playerStateSubscription != null && !playerStateSubscription.isCanceled()) {
            try { playerStateSubscription.cancel(); } catch (Throwable ignore) {}
            playerStateSubscription = null;
        }
        releaseMediaPlayer();
        if (spotifyAppRemote != null) {
            try { SpotifyAppRemote.disconnect(spotifyAppRemote); } catch (Exception ignore) {}
            spotifyAppRemote = null;
        }
        isPlaying.postValue(false);
        spotifyConnected.postValue(false);
    }

    @Override protected void onCleared() {
        releaseResources();
        super.onCleared();
    }
}
