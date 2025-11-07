package com.example.Apoloplay.ui.playlistdetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.List;

public class PlaylistDetailsViewModel extends ViewModel {

    private final PlaylistsRepository repo = ServiceLocator.playlistsRepository();

    private final MutableLiveData<PlaylistDetailsUiState> state =
            new MutableLiveData<>(PlaylistDetailsUiState.idle());

    public LiveData<PlaylistDetailsUiState> getState() { return state; }

    public void load(String playlistId) {
        state.postValue(PlaylistDetailsUiState.loading());
        repo.getPlaylistTracks(playlistId, 100, 0, new PlaylistsRepository.TracksCallback() {
            @Override public void onSuccess(List<Music> data) {
                state.postValue(PlaylistDetailsUiState.success(data));
            }
            @Override public void onError(String message) {
                state.postValue(PlaylistDetailsUiState.error(message));
            }
        });
    }

    public void add(String playlistId, String trackUri, Runnable onDone){
        repo.addTrackToPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                if (onDone != null) onDone.run();
            }
            @Override public void onError(String message) {

                state.postValue(PlaylistDetailsUiState.error(message));
            }
        });
    }

    public void remove(String playlistId, String trackUri, Runnable onDone){
        repo.removeTrackFromPlaylist(playlistId, trackUri, new PlaylistsRepository.SimpleCallback() {
            @Override public void onSuccess() {
                if (onDone != null) onDone.run();
            }
            @Override public void onError(String message) {
                state.postValue(PlaylistDetailsUiState.error(message));
            }
        });
    }
}
