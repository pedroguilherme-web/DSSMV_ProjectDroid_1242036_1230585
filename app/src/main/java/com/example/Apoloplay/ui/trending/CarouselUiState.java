package com.example.Apoloplay.ui.trending;

import com.example.Apoloplay.data.model.Music;
import java.util.Collections;
import java.util.List;


public final class CarouselUiState {
    private final List<Music> items;
    private final int currentIndex;
    private final boolean isAuto;

    private CarouselUiState(List<Music> items, int currentIndex, boolean isAuto) {
        this.items = (items != null) ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.currentIndex = Math.max(0, currentIndex);
        this.isAuto = isAuto;
    }

    public static CarouselUiState of(List<Music> items, int currentIndex, boolean isAuto) {
        return new CarouselUiState(items, currentIndex, isAuto);
    }
    public static CarouselUiState empty() { return new CarouselUiState(Collections.emptyList(), 0, false); }

    public List<Music> getItems() { return items; }
    public int getCurrentIndex() { return currentIndex; }
    public boolean isAuto() { return isAuto; }
}
