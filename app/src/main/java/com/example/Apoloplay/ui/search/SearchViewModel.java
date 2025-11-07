package com.example.Apoloplay.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.repository.SearchRepositoryImpl;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.SearchRepository;

import java.util.List;

public class SearchViewModel extends ViewModel {

    private final SearchRepository repo = new SearchRepositoryImpl();

    private final MutableLiveData<SearchUiState> state = new MutableLiveData<>(SearchUiState.idle());
    public LiveData<SearchUiState> getState(){ return state; }

    public void search(String query){
        state.postValue(SearchUiState.loading());
        repo.searchTracks(query, new SearchRepository.Callback() {
            @Override public void onSuccess(List<Music> data) {
                state.postValue(SearchUiState.success(data));
            }
            @Override public void onError(String message) {
                state.postValue(SearchUiState.error(message!=null?message:"Erro inesperado"));
            }
        });
    }
}
