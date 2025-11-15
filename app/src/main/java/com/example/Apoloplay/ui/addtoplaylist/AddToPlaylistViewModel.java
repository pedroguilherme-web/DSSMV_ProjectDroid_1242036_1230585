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

    private final MutableLiveData<AddToPlaylistUiState> _state =
            new MutableLiveData<>(AddToPlaylistUiState.idle());

    public LiveData<AddToPlaylistUiState> getState() {
        return _state;
    }

    // ----- Ações -----

    public void refresh() {
        _state.postValue(AddToPlaylistUiState.loading());
        repo.getMyPlaylists(50, 0, new PlaylistsRepository.PlaylistsCallback() {
            @Override public void onSuccess(List<Playlist> data) {
                _state.postValue(AddToPlaylistUiState.list(data));
            }

            @Override public void onError(String message) {
                _state.postValue(AddToPlaylistUiState.error(
                        message != null ? message : "Erro a carregar playlists"
                ));
            }
        });
    }

    public void create(String name) {
        // opcional: mostrar loading outra vez
        _state.postValue(AddToPlaylistUiState.loading());
        repo.createPlaylist(name, "", false, new PlaylistsRepository.PlaylistCallback() {
            @Override public void onSuccess(Playlist playlist) {
                // Avisamos a View que criou → ela faz refresh da lista
                _state.postValue(AddToPlaylistUiState.playlistCreated());
            }

            @Override public void onError(String message) {
                _state.postValue(AddToPlaylistUiState.error(
                        message != null ? message : "Erro a criar playlist"
                ));
            }
        });
    }

    public void addTrack(String playlistId, String trackUri) {
        _state.postValue(AddToPlaylistUiState.loading());
        repo.addTrackToPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                _state.postValue(AddToPlaylistUiState.added());
            }

            @Override public void onError(String message) {
                _state.postValue(AddToPlaylistUiState.error(
                        message != null ? message : "Erro a adicionar faixa"
                ));
            }
        });
    }
}
