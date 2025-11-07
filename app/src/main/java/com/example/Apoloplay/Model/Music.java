package com.example.Apoloplay.Model;

import java.io.Serializable;

public class Music implements Serializable {
    private final String title;
    private final String artist;
    private final String imageUrl;
    private final String spotifyTrackUri;
    private final String albumName;
    private final String releaseDate;
    private final String previewUrl; // 🎧 Novo campo

    public Music(String title, String artist, String imageUrl,
                 String spotifyTrackUri, String albumName,
                 String releaseDate, String previewUrl) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.spotifyTrackUri = spotifyTrackUri;
        this.albumName = albumName;
        this.releaseDate = releaseDate;
        this.previewUrl = previewUrl;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getSpotifyTrackUri() { return spotifyTrackUri; }
    public String getAlbumName() { return albumName; }
    public String getReleaseDate() { return releaseDate; }
    public String getPreviewUrl() { return previewUrl; } // 🎧 Getter novo
}
