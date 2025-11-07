package com.example.Apoloplay.data.remote.dto;



import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaylistsResponseDTO {
    @SerializedName("items") public List<PlaylistDTO> items;
    @SerializedName("total") public int total;
}
