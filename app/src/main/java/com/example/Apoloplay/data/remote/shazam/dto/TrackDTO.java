package com.example.Apoloplay.data.remote.shazam.dto;

public class TrackDTO {
    private String title;
    private String subtitle;
    private Images images;

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Images getImages() { return images; }

    public static class Images {
        private String coverart;
        public String getCoverart() { return coverart; }
    }
}
