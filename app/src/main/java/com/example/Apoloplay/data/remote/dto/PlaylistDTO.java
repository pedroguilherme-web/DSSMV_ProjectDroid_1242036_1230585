package com.example.Apoloplay.data.remote.dto;



import com.google.gson.annotations.SerializedName;

public class PlaylistDTO {
    @SerializedName("id")   public String id;
    @SerializedName("name") public String name;
    @SerializedName("tracks") public TracksRef tracks;

    public static class TracksRef {
        @SerializedName("total") public int total;
    }
}
