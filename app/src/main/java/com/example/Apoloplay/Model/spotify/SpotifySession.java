package com.example.Apoloplay.Model.spotify;

/** Singleton em memória para guardar o access token do utilizador (implicit flow). */
public final class SpotifySession {

    private static final SpotifySession INSTANCE = new SpotifySession();

    private String userAccessToken; // mantém-se enquanto o processo da app estiver vivo

    private SpotifySession() { }

    /** Acede à instância única. */
    public static SpotifySession getInstance() {
        return INSTANCE;
    }

    /** Define/atualiza o access token do utilizador. */
    public void setUserAccessToken(String token) {
        this.userAccessToken = token;
    }

    /** Lê o access token atual (pode ser null se ainda não fizeste login). */
    public String getUserAccessToken() {
        return userAccessToken;
    }

    /** Limpa a sessão (logout). */
    public void clear() {
        this.userAccessToken = null;
    }
}
