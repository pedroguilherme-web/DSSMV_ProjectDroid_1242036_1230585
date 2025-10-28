package com.example.Apoloplay.models;

public class Music {
    private String title;
    private String artist;
    private String imageUrl;

    public Music(String title, String artist, String imageUrl) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
}