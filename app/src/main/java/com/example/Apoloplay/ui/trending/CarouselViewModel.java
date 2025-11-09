// com/example/Apoloplay/ui/trending/CarouselViewModel.java
package com.example.Apoloplay.ui.trending;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.domain.model.Music;

import java.util.List;

/** VM guarda estado do carrossel (índice + timer de auto-scroll). */
public class CarouselViewModel extends ViewModel {

    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isAuto = new MutableLiveData<>(false);

    private final Handler handler = new Handler();
    private final int intervalMs = 2500;

    private List<Music> cached;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (cached == null || cached.isEmpty()) return;
            if (!Boolean.TRUE.equals(isAuto.getValue())) return;

            Integer cur = currentIndex.getValue();
            int next = ((cur != null ? cur : 0) + 1) % cached.size();
            currentIndex.setValue(next);
            handler.postDelayed(this, intervalMs);
        }
    };

    public LiveData<Integer> getCurrentIndex() { return currentIndex; }
    public LiveData<Boolean> getIsAuto() { return isAuto; }

    public void bindList(List<Music> list) {
        cached = list;
        if (cached == null || cached.isEmpty()) stopAuto();
    }

    public void setIndex(int idx) {
        if (cached == null || cached.isEmpty()) return;
        if (idx < 0) idx = 0;
        if (idx >= cached.size()) idx = cached.size() - 1;
        currentIndex.setValue(idx);
    }

    public void startAuto() {
        if (cached == null || cached.isEmpty()) return;
        if (Boolean.TRUE.equals(isAuto.getValue())) return;
        isAuto.setValue(true);
        handler.postDelayed(tick, intervalMs);
    }

    public void stopAuto() {
        isAuto.setValue(false);
        handler.removeCallbacks(tick);
    }

    @Override protected void onCleared() {
        handler.removeCallbacks(tick);
    }
}
