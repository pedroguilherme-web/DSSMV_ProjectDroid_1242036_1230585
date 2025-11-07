package com.example.Apoloplay.data.remote.dto;

public class CreatePlaylistRequest {
    public final String name;
    public final String description;
    public final boolean _public;
    public CreatePlaylistRequest(String name, String description, boolean isPublic) {
        this.name = name; this.description = description; this._public = isPublic;
    }
}
