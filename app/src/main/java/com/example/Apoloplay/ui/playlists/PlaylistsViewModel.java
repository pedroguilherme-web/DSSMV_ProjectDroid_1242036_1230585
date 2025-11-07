package com.example.Apoloplay.ui.playlists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel (arquitetura nova + domain repo):
 *  - expõe PlaylistsUiState (loading, items, error)
 *  - usa PlaylistsRepository (callbacks do domínio)
 *  - mantém sinais para o AddToPlaylistSheet (createdPlaylist / addTrackSuccess)
 */
public class PlaylistsViewModel extends ViewModel {

    private final PlaylistsRepository repo;

    private final MutableLiveData<PlaylistsUiState> state =
            new MutableLiveData<>(new PlaylistsUiState(false, Collections.emptyList(), null));
    public LiveData<PlaylistsUiState> getState() { return state; }

    // sinais auxiliares (BottomSheet)
    private final MutableLiveData<Playlist> createdPlaylist = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addTrackSuccess  = new MutableLiveData<>();
    private final MutableLiveData<String>  errorBus         = new MutableLiveData<>();

    public LiveData<Playlist> getCreatedPlaylist() { return createdPlaylist; }
    public LiveData<Boolean> getAddTrackSuccess()  { return addTrackSuccess; }
    public LiveData<String>  getError()           { return errorBus; }

    public PlaylistsViewModel() {
        this(ServiceLocator.playlistsRepository());
    }

    // visível para testes
    PlaylistsViewModel(PlaylistsRepository repo) {
        this.repo = repo;
    }

    // ================= AÇÕES =================

    /** Carrega / refresca as playlists do utilizador. */
    public void refresh() {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> keep = cur != null ? cur.items : Collections.emptyList();
        state.postValue(new PlaylistsUiState(true, keep, null));

        repo.getMyPlaylists(50, 0, new PlaylistsRepository.PlaylistsCallback() {
            @Override public void onSuccess(List<Playlist> items) {
                state.postValue(new PlaylistsUiState(false, items, null));
            }
            @Override public void onError(String message) {
                state.postValue(new PlaylistsUiState(false, keep, message));
            }
        });
    }

    /** Elimina uma playlist e atualiza imediatamente a lista local. */
    public void delete(String playlistId) {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> keep = cur != null ? cur.items : Collections.emptyList();
        state.postValue(new PlaylistsUiState(true, keep, null));

        repo.deletePlaylist(playlistId, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                List<Playlist> updated = new ArrayList<>(keep);
                for (int i = 0; i < updated.size(); i++) {
                    if (playlistId.equals(updated.get(i).id)) {
                        updated.remove(i);
                        break;
                    }
                }
                state.postValue(new PlaylistsUiState(false, updated, null));
            }
            @Override public void onError(String message) {
                state.postValue(new PlaylistsUiState(false, keep, message));
            }
        });
    }

    /** Cria uma playlist (para o BottomSheet). */
    public void createPlaylist(String name) {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> keep = cur != null ? cur.items : Collections.emptyList();
        state.postValue(new PlaylistsUiState(true, keep, null));

        repo.createPlaylist(name, "Criada via Apoloplay", false,
                new PlaylistsRepository.PlaylistCallback() {
                    @Override public void onSuccess(Playlist playlist) {
                        state.postValue(new PlaylistsUiState(false, keep, null));
                        createdPlaylist.postValue(playlist);
                    }
                    @Override public void onError(String message) {
                        state.postValue(new PlaylistsUiState(false, keep, null));
                        errorBus.postValue(message);
                    }
                });
    }

    /** Adiciona 1 faixa ("spotify:track:ID") à playlist. */
    public void addTrackToPlaylist(String playlistId, String trackUri) {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> keep = cur != null ? cur.items : Collections.emptyList();
        state.postValue(new PlaylistsUiState(true, keep, null));

        repo.addTrackToPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                state.postValue(new PlaylistsUiState(false, keep, null));
                addTrackSuccess.postValue(true);
            }
            @Override public void onError(String message) {
                state.postValue(new PlaylistsUiState(false, keep, null));
                errorBus.postValue(message);
            }
        });
    }

    /** Remove 1 faixa (podes chamar no “roda dentada” dos detalhes). */
    public void removeTrackFromPlaylist(String playlistId, String trackUri, Runnable onDone) {
        PlaylistsUiState cur = state.getValue();
        List<Playlist> keep = cur != null ? cur.items : Collections.emptyList();
        state.postValue(new PlaylistsUiState(true, keep, null));

        repo.removeTrackFromPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                state.postValue(new PlaylistsUiState(false, keep, null));
                if (onDone != null) onDone.run();
            }
            @Override public void onError(String message) {
                state.postValue(new PlaylistsUiState(false, keep, message));
                if (onDone != null) onDone.run();
            }
        });
    }
}
