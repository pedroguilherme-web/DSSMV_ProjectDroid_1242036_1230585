package com.example.Apoloplay.data.repository.interfaces;

import com.example.Apoloplay.data.model.Music;
import java.util.List;

public interface SearchRepository {
    interface Callback {
        void onSuccess(List<Music> data);
        void onError(String message);
    }
    void searchTracks(String query, Callback cb);
}
