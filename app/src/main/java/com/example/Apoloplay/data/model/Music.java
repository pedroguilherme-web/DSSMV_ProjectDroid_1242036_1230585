package com.example.Apoloplay.data.model;

import java.io.Serializable;


public class Music implements Serializable {


    public final String title;
    public final String artist;
    public final String imageUrl;
    public final String previewUrl;
    public final String albumName;
    public final String releaseDate;
    public final String spotifyTrackUri;



    public Music( String title, String artist, String imageUrl, String previewUrl,
                  String albumName, String releaseDate, String spotifyTrackUri) {

        this.title = title;
        this.artist = artist;

        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
        this.albumName = albumName;
        this.spotifyTrackUri = spotifyTrackUri;
        this.releaseDate = releaseDate;
    }


    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getPreviewUrl() { return previewUrl; }
    public String getAlbumName() { return albumName; }
    public String getSpotifyTrackUri() { return spotifyTrackUri; }
    public String getReleaseDate() { return releaseDate; }

    // aliases que a tua UI antiga usava


    @Override public String toString() {
        return "Music{title='" + title + "', artist='" + artist + "', album='" + albumName + "'}";
    }
}
