package com.example.Apoloplay.ViewModel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.Apoloplay.Model.Music;
import com.example.Apoloplay.Model.repository.SpotifyRepository;

import java.util.List;

/**
 * ViewModel: Lógica de UI. Prepara os dados do Repositório para a View.
 */
public class MusicViewModel extends ViewModel {

    private final SpotifyRepository repository;
    private final MutableLiveData<List<Music>> musicResults = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MusicViewModel() {
        // A ViewModel pede dados ao Repositório
        repository = new SpotifyRepository();
    }

    /**
     * Expõe a lista de Músicas à Activity/Fragment (View)
     * A View irá 'observar' esta lista.
     */
    public LiveData<List<Music>> getMusicResults() {
        return musicResults;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Acção para iniciar a busca. Chamada pela Activity.
     */
    public void search(String query) {
        isLoading.setValue(true);
        // O Repositório faz o trabalho pesado (Token + Busca)
        repository.searchTracks(query, musicResults);
    }

    /**
     * Quando o Repositório devolve o resultado (sucesso ou falha),
     * o LiveData é atualizado, e precisamos de desligar o loading.
     */
    public void onDataReady() {
        isLoading.setValue(false);
    }

    @Override
    protected void onCleared() {
        // Lógica de limpeza se necessário (ex: cancelar chamadas Retrofit pendentes)
        super.onCleared();
    }
}