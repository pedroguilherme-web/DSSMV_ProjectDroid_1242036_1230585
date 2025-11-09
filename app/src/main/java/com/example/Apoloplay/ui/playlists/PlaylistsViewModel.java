package com.example.Apoloplay.ui.playlists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.List;

public class PlaylistsViewModel extends ViewModel {

    private static final String TAG = "PlaylistsVM";
    private final PlaylistsRepository repository;

    // ÚNICO estado observado pela UI
    private final MutableLiveData<PlaylistsUiState> _uiState =
            new MutableLiveData<>(PlaylistsUiState.loading());
    public final LiveData<PlaylistsUiState> uiState = _uiState;

    // Eventos/sinais
    private final MutableLiveData<Boolean> _addTrackSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> addTrackSuccess = _addTrackSuccess;

    private final MutableLiveData<Playlist> _createdPlaylist = new MutableLiveData<>();
    public final LiveData<Playlist> createdPlaylist = _createdPlaylist;

    public PlaylistsViewModel() { this(ServiceLocator.playlistsRepository()); }
    public PlaylistsViewModel(PlaylistsRepository repository) { this.repository = repository; }

    public void loadMyPlaylists(int limit, int offset) {
        _uiState.postValue(PlaylistsUiState.loading());

        repository.getMyPlaylists(limit, offset, new PlaylistsRepository.PlaylistsCallback() {
            @Override public void onSuccess(List<Playlist> playlists) {
                _uiState.postValue(PlaylistsUiState.data(playlists));
            }
            @Override public void onError(String message) {
                _uiState.postValue(PlaylistsUiState.error(message));
            }
        });
    }

    public void refresh() { loadMyPlaylists(20, 0); }

    public void createPlaylist(String name, String description, boolean isPublic) {
        repository.createPlaylist(name, description, isPublic, new PlaylistsRepository.PlaylistCallback() {
            @Override public void onSuccess(Playlist playlist) { _createdPlaylist.postValue(playlist); }
            @Override public void onError(String message) { _uiState.postValue(PlaylistsUiState.error(message)); }
        });
    }

    public void addTrack(String playlistId, String trackUri) {
        repository.addTrackToPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() { _addTrackSuccess.postValue(true); }
            @Override public void onError(String message) {
                _addTrackSuccess.postValue(false);
                _uiState.postValue(PlaylistsUiState.error(message));
            }
        });
    }

    public void delete(String playlistId) {
        repository.deletePlaylist(playlistId, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() { refresh(); }
            @Override public void onError(String message) { _uiState.postValue(PlaylistsUiState.error(message)); }
        });
    }

    @Override protected void onCleared() {
        Log.d(TAG, "onCleared: cancelAll");
        repository.cancelAll();
        super.onCleared();
    }
}
