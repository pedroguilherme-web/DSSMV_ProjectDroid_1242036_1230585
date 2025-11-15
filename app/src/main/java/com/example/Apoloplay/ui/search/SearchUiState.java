package com.example.Apoloplay.ui.search;

import androidx.annotation.Nullable;
import com.example.Apoloplay.data.model.Music;
import java.util.Collections;
import java.util.List;

public final class SearchUiState {
    public enum Status { IDLE, LOADING, DATA, ERROR }

    private final Status status;
    private final List<Music> results;
    private final String error;

    private SearchUiState(Status s, @Nullable List<Music> data, @Nullable String err) {
        this.status = s;
        this.results = (data != null) ? Collections.unmodifiableList(data) : Collections.emptyList();
        this.error = err;
    }

    public static SearchUiState idle()    { return new SearchUiState(Status.IDLE, null, null); }
    public static SearchUiState loading() { return new SearchUiState(Status.LOADING, null, null); }
    public static SearchUiState data(List<Music> d)   { return new SearchUiState(Status.DATA, d, null); }
    public static SearchUiState error(String e)       { return new SearchUiState(Status.ERROR, null, e); }

    public Status getStatus() { return status; }
    public List<Music> getResults() { return results; }
    public String getError() { return error; }
}
