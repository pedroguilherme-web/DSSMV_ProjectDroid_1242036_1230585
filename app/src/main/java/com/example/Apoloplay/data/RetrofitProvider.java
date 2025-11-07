package com.example.Apoloplay.data;

import com.example.Apoloplay.data.remote.SpotifyService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Responsável por fornecer instâncias Retrofit
 * tanto para o endpoint de autenticação como para o endpoint da API principal.
 */
public final class RetrofitProvider {

    public static final String AUTH_BASE_URL = "https://accounts.spotify.com/";
    public static final String API_BASE_URL  = "https://api.spotify.com/v1/";

    private static Retrofit authRetrofit;
    private static Retrofit apiRetrofit;

    private RetrofitProvider() {
        // impedir instanciação
    }

    /** Retrofit para endpoints de autenticação (token, etc.) */
    public static Retrofit provideAuthRetrofit() {
        if (authRetrofit == null) {
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(AUTH_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    /** Retrofit para endpoints da API principal (playlists, tracks, etc.) */
    public static Retrofit provideApiRetrofit() {
        if (apiRetrofit == null) {
            apiRetrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return apiRetrofit;
    }

    /** Serviço padrão (API principal) */
    public static SpotifyService provideSpotifyService() {
        return provideApiRetrofit().create(SpotifyService.class);
    }
}
