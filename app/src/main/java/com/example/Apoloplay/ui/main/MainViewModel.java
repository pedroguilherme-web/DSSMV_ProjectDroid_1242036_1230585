package com.example.Apoloplay.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.TrendingRepository;

import java.util.List;

/**
 * ViewModel responsável por fornecer as músicas em tendência
 * (carrossel da MainActivity). Mantém a lista viva e isolada
 * da lógica de rede e API (MVVM).
 */
public class MainViewModel extends ViewModel {

    private final TrendingRepository repository = new TrendingRepository();

    private final MutableLiveData<List<Music>> trending = new MutableLiveData<>();

    private final android.os.Handler handler = new android.os.Handler();
    private final int intervalMs = 3000;
    private Runnable autoScroll;

    public void startAutoScroll(Runnable scrollAction) {
        autoScroll = () -> {
            scrollAction.run();
            handler.postDelayed(autoScroll, intervalMs);
        };
        handler.postDelayed(autoScroll, intervalMs);
    }

    public void stopAutoScroll() {
        handler.removeCallbacks(autoScroll);
    }


    public LiveData<List<Music>> getTrending() {
        return trending;
    }

    /** Chama o repositório para obter as faixas em tendência (Top 100 Global). */
    public void loadTrending() {
        repository.getTrending(trending::postValue);
    }
}
