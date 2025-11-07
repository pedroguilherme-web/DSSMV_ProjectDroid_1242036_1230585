package com.example.Apoloplay.Model;





import com.google.gson.annotations.SerializedName;

public class UserProfile {

    @SerializedName("id")
    private String id;

    @SerializedName("display_name")
    private String displayName;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
