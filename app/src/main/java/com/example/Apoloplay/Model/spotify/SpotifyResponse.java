package com.example.Apoloplay.Model.spotify;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyResponse {

    @SerializedName("tracks")
    private Tracks tracks;

    public Tracks getTracks() {
        return tracks;
    }

    // --- CLASSES ANINHADAS PARA MAPEAMENTO JSON ---

    public static class Tracks {
        @SerializedName("items")
        private List<TrackItem> items;

        public List<TrackItem> getItems() {
            return items;
        }
    }

    public static class TrackItem {
        @SerializedName("name")
        private String name;
        @SerializedName("artists")
        private List<Artist> artists;
        @SerializedName("album")
        private Album album;

        @SerializedName("preview_url")
        private String previewUrl;

        public String getPreviewUrl() {
            return previewUrl;
        }

        @SerializedName("uri")
        private String uri;

        public String getName() {
            return name;
        }
        public List<Artist> getArtists() {
            return artists;
        }
        public Album getAlbum() {
            return album;
        }
        public String getUri() {
            return uri;
        }
    }

    public static class Artist {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class Album {
        @SerializedName("images")
        private List<Image> images;

        @SerializedName("name")
        private String name;
        @SerializedName("release_date")
        private String releaseDate;

        public List<Image> getImages() {
            return images;
        }
        public String getName() {
            return name;
        }
        public String getReleaseDate() {
            return releaseDate;
        }
    }

    public static class Image {
        @SerializedName("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }
}