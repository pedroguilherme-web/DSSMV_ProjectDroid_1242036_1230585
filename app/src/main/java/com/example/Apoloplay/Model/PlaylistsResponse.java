package com.example.Apoloplay.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;


public class PlaylistsResponse {

    @SerializedName("items")
    private List<Playlist> items;

    @SerializedName("total")
    private int total;

    public List<Playlist> getItems() {
        return items;
    }

    public void setItems(List<Playlist> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
