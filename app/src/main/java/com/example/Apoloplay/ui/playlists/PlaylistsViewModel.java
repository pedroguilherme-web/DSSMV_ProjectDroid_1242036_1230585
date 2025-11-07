package com.example.Apoloplay.ui.playlists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsViewModel extends ViewModel {

    private final PlaylistsRepository repo = ServiceLocator.playlistsRepository();

    private final MutableLiveData<PlaylistsUiState> state = new MutableLiveData<>(PlaylistsUiState.idle());
    public LiveData<PlaylistsUiState> getState() { return state; }

    public void refresh() {
        state.postValue(PlaylistsUiState.loading());
        repo.getMyPlaylists(50, 0, new PlaylistsRepository.PlaylistsCallback() {
            @Override public void onSuccess(List<Playlist> data) {
                state.postValue(PlaylistsUiState.success(data));
            }
            @Override public void onError(String message) {
                state.postValue(PlaylistsUiState.error(message));
            }
        });
    }

    /** Atualização otimista: remove já da lista, confirma com API, e reverte em caso de erro. */
    public void delete(String playlistId) {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> current = (cur!=null && cur.items!=null) ? cur.items : new ArrayList<>();

        // snapshot p/ rollback
        List<Playlist> snapshot = new ArrayList<>(current);

        // otimista
        List<Playlist> updated = new ArrayList<>();
        for (Playlist p : current) if (!p.id.equals(playlistId)) updated.add(p);
        state.postValue(new PlaylistsUiState(false, updated, null));

        repo.deletePlaylist(playlistId, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                // tudo bem
            }
            @Override public void onError(String message) {
                // rollback e erro
                state.postValue(new PlaylistsUiState(false, snapshot, message));
            }
        });
    }
}
