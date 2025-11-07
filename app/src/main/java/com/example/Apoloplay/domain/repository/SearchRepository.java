package com.example.Apoloplay.domain.repository;

import com.example.Apoloplay.domain.model.Music;
import java.util.List;

public interface SearchRepository {
    interface Callback {
        void onSuccess(List<Music> data);
        void onError(String message);
    }
    void searchTracks(String query, Callback cb);
}
