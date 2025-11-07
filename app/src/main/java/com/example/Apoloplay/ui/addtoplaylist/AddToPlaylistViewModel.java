package com.example.Apoloplay.ui.addtoplaylist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.List;

public class AddToPlaylistViewModel extends ViewModel {

    private final PlaylistsRepository repo = ServiceLocator.playlistsRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Playlist>> playlists = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Playlist> created = new MutableLiveData<>();

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<List<Playlist>> getPlaylists() { return playlists; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getAddSuccess() { return addSuccess; }
    public LiveData<Playlist> getCreatedPlaylist() { return created; }

    public void refresh() {
        loading.postValue(true);
        error.postValue(null);
        repo.getMyPlaylists(50, 0, new PlaylistsRepository.PlaylistsCallback() {
            @Override public void onSuccess(List<Playlist> data) {
                loading.postValue(false);
                playlists.postValue(data);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Erro a carregar playlists");
            }
        });
    }

    public void create(String name) {
        loading.postValue(true);
        error.postValue(null);
        repo.createPlaylist(name, /*desc*/"", /*public*/false, new PlaylistsRepository.PlaylistCallback() {
            @Override public void onSuccess(Playlist playlist) {
                loading.postValue(false);
                created.postValue(playlist);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Erro a criar playlist");
            }
        });
    }

    public void addTrack(String playlistId, String trackUri) {
        loading.postValue(true);
        error.postValue(null);
        repo.addTrackToPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                loading.postValue(false);
                addSuccess.postValue(true);
            }
            @Override public void onError(String message) {
                loading.postValue(false);
                error.postValue(message != null ? message : "Erro a adicionar faixa");
            }
        });
    }
}
