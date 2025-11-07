package com.example.Apoloplay.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SpotifyAlbumDTO {
    @SerializedName("name") public String name;
    @SerializedName("release_date") public String releaseDate;
    @SerializedName("images") public java.util.List<SpotifyImageDTO> images;
}
