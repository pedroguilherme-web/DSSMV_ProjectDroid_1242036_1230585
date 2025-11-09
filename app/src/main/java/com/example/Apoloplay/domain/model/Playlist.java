package com.example.Apoloplay.domain.model;

/** Domínio: Playlist (encapsulado, imutável) */
public class Playlist {

    private final String id;
    private final String name;
    private final int tracksTotal;

    public Playlist(String id, String name, int tracksTotal) {
        this.id = id;
        this.name = name;
        this.tracksTotal = tracksTotal;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getTracksTotal() { return tracksTotal; }

    @Override public String toString() {
        return "Playlist{id='" + id + "', name='" + name + "', tracksTotal=" + tracksTotal + "}";
    }
}
