package com.example.Apoloplay.data.repository.interfaces;

import com.example.Apoloplay.data.model.Music;

import java.util.List;
import java.util.function.Consumer;

public interface TrendingRepository {

    void getTrending(Consumer<List<Music>> callback);
}
