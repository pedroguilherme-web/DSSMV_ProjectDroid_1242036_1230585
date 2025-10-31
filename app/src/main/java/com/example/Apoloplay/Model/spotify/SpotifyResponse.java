package com.example.Apoloplay.Model.spotify;


import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Mapeia a resposta JSON completa do endpoint /search do Spotify.
 * A estrutura foi simplificada para incluir apenas os campos essenciais que você utiliza.
 */
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

        public String getName() {
            return name;
        }

        public List<Artist> getArtists() {
            return artists;
        }

        public Album getAlbum() {
            return album;
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

        public List<Image> getImages() {
            return images;
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