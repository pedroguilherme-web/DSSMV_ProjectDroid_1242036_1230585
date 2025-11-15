package com.example.Apoloplay.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.repository.implementation.SearchRepositoryImpl;
import com.example.Apoloplay.data.model.Music;
import com.example.Apoloplay.data.repository.interfaces.SearchRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {

    private final SearchRepository repo = new SearchRepositoryImpl();

    private final MutableLiveData<SearchUiState> state = new MutableLiveData<>(SearchUiState.idle());
    public LiveData<SearchUiState> getState(){ return state; }

    public void search(String query){
        if (query == null || query.trim().isEmpty()) {
            state.postValue(SearchUiState.idle());
            return;
        }
        state.postValue(SearchUiState.loading());
        repo.searchTracks(query.trim(), new SearchRepository.Callback() {
            @Override public void onSuccess(List<Music> data) {
                state.postValue(SearchUiState.data(data));   // <= enum version usa data(...)
            }
            @Override public void onError(String message) {
                state.postValue(SearchUiState.error(message != null ? message : "Erro inesperado"));
            }
        });
    }
}
