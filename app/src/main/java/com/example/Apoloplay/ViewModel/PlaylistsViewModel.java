package com.example.Apoloplay.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.Model.AddTracksResponse;
import com.example.Apoloplay.Model.CreatePlaylistRequest;
import com.example.Apoloplay.Model.Playlist;
import com.example.Apoloplay.Model.PlaylistsResponse;
import com.example.Apoloplay.Model.UserProfile;
import com.example.Apoloplay.Model.repository.SpotifyRepository;
import com.example.Apoloplay.Model.spotify.SpotifySession;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistsViewModel extends ViewModel {

    private final SpotifyRepository repository = new SpotifyRepository();

    private final MutableLiveData<List<Playlist>> playlists = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Playlist> createdPlaylist = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addTrackSuccess = new MutableLiveData<>();

    private String cachedUserId = null;

    public LiveData<List<Playlist>> getPlaylists() { return playlists; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Playlist> getCreatedPlaylist() { return createdPlaylist; }
    public LiveData<Boolean> getAddTrackSuccess() { return addTrackSuccess; }

    private boolean ensureUserToken() {
        String token = SpotifySession.getInstance().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            error.postValue("Inicia sessão no Spotify para aceder às playlists.");
            return false;
        }
        repository.setAccessToken(token);
        return true;
    }

    public void loadMyPlaylists() {
        if (!ensureUserToken()) return;
        loading.postValue(true);
        error.postValue(null);

        repository.getMyPlaylists(50).enqueue(new Callback<PlaylistsResponse>() {
            @Override
            public void onResponse(Call<PlaylistsResponse> call, Response<PlaylistsResponse> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body()!=null) {
                    playlists.postValue(response.body().getItems());
                } else {
                    error.postValue("Erro a carregar playlists ("+response.code()+")");
                }
            }
            @Override
            public void onFailure(Call<PlaylistsResponse> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t != null ? t.getMessage() : "Falha de rede");
            }
        });
    }

    public void createPlaylist(String name) {
        if (!ensureUserToken()) return;
        loading.postValue(true);
        error.postValue(null);

        // precisamos do userId: usa /me (cache simples)
        if (cachedUserId != null) {
            doCreate(cachedUserId, name);
            return;
        }

        repository.getMe().enqueue(new Callback<UserProfile>() {
            @Override public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    cachedUserId = response.body().getId();
                    doCreate(cachedUserId, name);
                } else {
                    loading.postValue(false);
                    error.postValue("Erro a obter utilizador ("+response.code()+")");
                }
            }
            @Override public void onFailure(Call<UserProfile> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t != null ? t.getMessage() : "Falha de rede");
            }
        });
    }

    private void doCreate(String userId, String name) {
        CreatePlaylistRequest body = new CreatePlaylistRequest(name, false, false,
                "Criada via Apoloplay");
        repository.createPlaylist(userId, body).enqueue(new Callback<Playlist>() {
            @Override public void onResponse(Call<Playlist> call, Response<Playlist> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body()!=null) {
                    createdPlaylist.postValue(response.body());
                } else {
                    error.postValue("Erro a criar playlist ("+response.code()+")");
                }
            }
            @Override public void onFailure(Call<Playlist> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t != null ? t.getMessage() : "Falha de rede");
            }
        });
    }

    /** trackUri pode ser "spotify:track:ID" (um único). */
    public void addTrackToPlaylist(String playlistId, String trackUri) {
        if (!ensureUserToken()) return;
        loading.postValue(true);
        error.postValue(null);

        repository.addTracks(playlistId, trackUri).enqueue(new Callback<AddTracksResponse>() {
            @Override public void onResponse(Call<AddTracksResponse> call, Response<AddTracksResponse> response) {
                loading.postValue(false);
                if (response.isSuccessful()) {
                    addTrackSuccess.postValue(true);
                } else {
                    error.postValue("Erro a adicionar faixa ("+response.code()+")");
                }
            }
            @Override public void onFailure(Call<AddTracksResponse> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t != null ? t.getMessage() : "Falha de rede");
            }
        });
    }
}
