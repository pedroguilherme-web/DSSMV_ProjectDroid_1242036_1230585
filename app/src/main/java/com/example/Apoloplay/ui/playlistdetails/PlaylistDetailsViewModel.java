package com.example.Apoloplay.ui.playlistdetails;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.data.model.Music;
import com.example.Apoloplay.data.repository.interfaces.PlaylistsRepository;

import java.util.List;

/**
 * VM de detalhes da playlist.
 * - expõe um único estado: LiveData<PlaylistDetailsUiState>
 * - carrega faixas da playlist (repo.getPlaylistTracks)
 * - remove faixa da playlist (repo.removeTrackFromPlaylist)
 * - cancela chamadas pendentes no onCleared()
 */
public class PlaylistDetailsViewModel extends ViewModel {

    private static final String TAG = "PlaylistDetailsVM";

    private final PlaylistsRepository repo;

    private final MutableLiveData<PlaylistDetailsUiState> _state =
            new MutableLiveData<>(PlaylistDetailsUiState.loading());
    public LiveData<PlaylistDetailsUiState> getState() { return _state; }

    public PlaylistDetailsViewModel() {
        this(ServiceLocator.playlistsRepository());
    }

    public PlaylistDetailsViewModel(PlaylistsRepository repo) {
        this.repo = repo;
    }

    /** Carrega as faixas da playlist (uma página simples). */
    public void load(String playlistId) {
        if (playlistId == null || playlistId.isEmpty()) {
            _state.postValue(PlaylistDetailsUiState.error("ID de playlist inválido."));
            return;
        }
        _state.postValue(PlaylistDetailsUiState.loading());
        repo.getPlaylistTracks(playlistId, 100, 0, new PlaylistsRepository.TracksCallback() {
            @Override public void onSuccess(List<Music> tracks) {
                _state.postValue(PlaylistDetailsUiState.data(tracks));
            }
            @Override public void onError(String message) {
                _state.postValue(PlaylistDetailsUiState.error(message));
            }
        });
    }

    /**
     * Remove uma faixa da playlist.
     * Mantive a assinatura com callback porque já a usas no DetailsActivity:
     * playlistVm.remove(playlistId, trackUri, () -> { ... })
     */
    public void remove(String playlistId, String trackUri, Runnable onSuccess) {
        if (playlistId == null || trackUri == null) return;
        repo.removeTrackFromPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                Log.d(TAG, "remove: success");
                if (onSuccess != null) onSuccess.run();
                // Opcional: recarregar a lista depois de remover
                load(playlistId);
            }
            @Override public void onError(String message) {
                Log.d(TAG, "remove: error=" + message);
                _state.postValue(PlaylistDetailsUiState.error(message));
            }
        });
    }

    @Override protected void onCleared() {
        repo.cancelAll();
        super.onCleared();
    }
}
