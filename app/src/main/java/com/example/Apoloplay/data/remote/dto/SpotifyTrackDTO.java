package com.example.Apoloplay.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyTrackDTO {
    @SerializedName("id") public String id;
    @SerializedName("uri") public String uri;
    @SerializedName("name") public String name;
    @SerializedName("preview_url") public String previewUrl;
    @SerializedName("artists") public List<SpotifyArtistDTO> artists;
    @SerializedName("album") public SpotifyAlbumDTO album;
}
