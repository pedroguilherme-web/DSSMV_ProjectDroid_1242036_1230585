package com.example.Apoloplay.data.remote.dto.search;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifySearchResponseDTO {
    @SerializedName("tracks") public Tracks tracks;

    public static class Tracks {
        @SerializedName("items") public List<Item> items;
    }

    public static class Item {
        @SerializedName("name") public String name;
        @SerializedName("uri") public String uri;                   // "spotify:track:..."
        @SerializedName("preview_url") public String previewUrl;
        @SerializedName("artists") public List<Artist> artists;
        @SerializedName("album") public Album album;
    }

    public static class Artist {
        @SerializedName("name") public String name;
    }

    public static class Album {
        @SerializedName("name") public String name;
        @SerializedName("release_date") public String releaseDate;
        @SerializedName("images") public List<Image> images;
    }

    public static class Image {
        @SerializedName("url") public String url;
    }
}
