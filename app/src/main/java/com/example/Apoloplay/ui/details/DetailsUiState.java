package com.example.Apoloplay.ui.details;

import com.example.Apoloplay.data.model.Music;

public class DetailsUiState {
    public enum Status { IDLE, LOADING, DATA, ERROR }

    public final Status status;
    public final Music music;          // pode ser “crua” (só title/artist) ou enriquecida
    public final boolean inPlaylist;   // se já existe na playlist atual (se fornecida)
    public final String error;

    private DetailsUiState(Status s, Music m, boolean in, String e){
        this.status=s; this.music=m; this.inPlaylist=in; this.error=e;
    }
    public static DetailsUiState idle(Music m){ return new DetailsUiState(Status.IDLE,m,false,null); }
    public static DetailsUiState loading(Music m){ return new DetailsUiState(Status.LOADING,m,false,null); }
    public static DetailsUiState data(Music m, boolean in){ return new DetailsUiState(Status.DATA,m,in,null); }
    public static DetailsUiState error(Music m,String e){ return new DetailsUiState(Status.ERROR,m,false,e); }
}
