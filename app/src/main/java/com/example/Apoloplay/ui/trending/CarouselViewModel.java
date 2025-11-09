package com.example.Apoloplay.ui.trending;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.domain.model.Music;

import java.util.List;


public class CarouselViewModel extends ViewModel {

    private final MutableLiveData<CarouselUiState> _state =
            new MutableLiveData<>(CarouselUiState.empty());
    public LiveData<CarouselUiState> getState() { return _state; }

    private List<Music> cached;
    private final Handler handler = new Handler();
    private static final int INTERVAL_MS = 2500;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            CarouselUiState s = _state.getValue();
            if (s == null || cached == null || cached.isEmpty()) return;
            if (!s.isAuto()) return;

            int next = (s.getCurrentIndex() + 1) % cached.size();
            _state.setValue(CarouselUiState.of(cached, next, true));
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    /** Liga/atualiza a lista (vinda do MainViewModel). */
    public void bindList(List<Music> list) {
        cached = list;
        CarouselUiState cur = _state.getValue();
        int idx   = (cur != null) ? cur.getCurrentIndex() : 0;
        boolean a = (cur != null) && cur.isAuto();

        if (cached == null || cached.isEmpty()) {
            stopAuto();
            _state.setValue(CarouselUiState.empty());
        } else {
            if (idx >= cached.size()) idx = 0;
            _state.setValue(CarouselUiState.of(cached, idx, a));
        }
    }

    public void setIndex(int idx) {
        if (cached == null || cached.isEmpty()) return;
        if (idx < 0) idx = 0;
        if (idx >= cached.size()) idx = cached.size() - 1;
        CarouselUiState cur = _state.getValue();
        boolean a = (cur != null) && cur.isAuto();
        _state.setValue(CarouselUiState.of(cached, idx, a));
    }

    public void startAuto() {
        if (cached == null || cached.isEmpty()) return;
        CarouselUiState cur = _state.getValue();
        if (cur != null && cur.isAuto()) return;
        int idx = (cur != null) ? cur.getCurrentIndex() : 0;
        _state.setValue(CarouselUiState.of(cached, idx, true));
        handler.postDelayed(tick, INTERVAL_MS);
    }

    public void stopAuto() {
        CarouselUiState cur = _state.getValue();
        if (cur != null) _state.setValue(CarouselUiState.of(cur.getItems(), cur.getCurrentIndex(), false));
        handler.removeCallbacks(tick);
    }

    @Override protected void onCleared() {
        handler.removeCallbacks(tick);
    }
}
