package com.example.Apoloplay.Model.auth.model;

// package com.example.Apoloplay.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mapeia a resposta JSON do pedido de Token de Acesso.
 */
public class TokenResponse {

    @SerializedName("access_token")
    private String accessToken; // O Token real que usaremos para fazer as buscas

    @SerializedName("token_type")
    private String tokenType; // Deve ser "Bearer"

    @SerializedName("expires_in")
    private int expiresIn; // Tempo de validade (em segundos)

    // --- Getters (Retrofit/GSON usa isto para desserializar) ---
    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}