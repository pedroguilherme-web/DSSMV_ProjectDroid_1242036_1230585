package com.example.Apoloplay.data;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Retrofit service interface para a API do Spotify.
 */
public interface SpotifyService {

    @GET("/v1/search")
    Call<SpotifyResponse> searchTracks(
            @Header("Authorization") String authHeader,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );
}

/**
 * Classes de modelo usadas na resposta do Spotify API.
 */
class SpotifyResponse {
    private Tracks tracks;

    public Tracks getTracks() {
        return tracks;
    }
}

class Tracks {
    private List<Track> items;

    public List<Track> getItems() {
        return items;
    }
}

class Track {
    private String name;
    private List<Artist> artists;
    private Album album;

    public String getName() { return name; }
    public List<Artist> getArtists() { return artists; }
    public Album getAlbum() { return album; }
}

class Artist {
    private String name;

    public String getName() { return name; }
}

class Album {
    private List<Image> images;

    public List<Image> getImages() { return images; }
}

class Image {
    private String url;

    public String getUrl() { return url; }
}