package com.example.Apoloplay.ui.search;

import com.example.Apoloplay.domain.model.Music;
import java.util.Collections;
import java.util.List;

public class SearchUiState {
    public final boolean loading;
    public final List<Music> items;
    public final String error;

    private SearchUiState(boolean l, List<Music> i, String e){
        loading = l; items = i; error = e;
    }
    public static SearchUiState idle(){ return new SearchUiState(false, Collections.emptyList(), null); }
    public static SearchUiState loading(){ return new SearchUiState(true, Collections.emptyList(), null); }
    public static SearchUiState success(List<Music> items){ return new SearchUiState(false, items, null); }
    public static SearchUiState error(String msg){ return new SearchUiState(false, Collections.emptyList(), msg); }
}
