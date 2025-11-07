package com.example.Apoloplay.Model;



public class CreatePlaylistRequest {
    public String name;
    public boolean public_;
    public boolean collaborative;
    public String description;

    public CreatePlaylistRequest(String name, boolean isPublic, boolean collaborative, String desc) {
        this.name = name;
        this.public_ = isPublic;
        this.collaborative = collaborative;
        this.description = desc;
    }
}
