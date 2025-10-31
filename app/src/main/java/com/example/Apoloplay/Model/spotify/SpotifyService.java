package com.example.Apoloplay.Model.spotify;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Interface Retrofit para a API de Busca do Spotify (Web API)
 */
public interface SpotifyService {

    // Endpoint: https://api.spotify.com/v1/search
    @GET("search")
    Call<SpotifyResponse> searchTracks(
            // Token de Autorização Bearer
            @Header("Authorization") String authorization,
            // Query de busca (o nome do artista ou música)
            @Query("q") String query,
            // Tipo de item a buscar (e.g., "track", "artist")
            @Query("type") String type,
            // Número máximo de resultados
            @Query("limit") int limit
    );
}