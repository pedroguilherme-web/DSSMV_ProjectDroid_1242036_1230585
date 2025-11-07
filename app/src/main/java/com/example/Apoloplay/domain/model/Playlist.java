package com.example.Apoloplay.domain.model;


public class Playlist {
    public final String id;
    public final String name;
    public final int tracksTotal;

    public Playlist(String id, String name, int tracksTotal) {
        this.id = id;
        this.name = name;
        this.tracksTotal = tracksTotal;
    }
}
