package com.example.Apoloplay.data.remote.dto;


import com.google.gson.annotations.SerializedName;

// Opcional — só se fores usar /browse/categories/{id}/playlists
public class CategoryPlaylistsDTO {
    @SerializedName("playlists") public PlaylistsResponseDTO playlists;
}
