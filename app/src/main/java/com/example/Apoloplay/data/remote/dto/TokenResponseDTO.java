package com.example.Apoloplay.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TokenResponseDTO {
    @SerializedName("access_token") public String accessToken;
    @SerializedName("token_type")   public String tokenType;
    @SerializedName("expires_in")   public long   expiresIn;
}
