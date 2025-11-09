package com.example.Apoloplay.data.model;

public class Track {
    private String title;
    private String subtitle;
    private Images images;

    // Getters
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Images getImages() { return images; }

    // Subclasse para URLs de imagem
    public static class Images {
        private String coverart;
        public String getCoverart() { return coverart; }
    }
}