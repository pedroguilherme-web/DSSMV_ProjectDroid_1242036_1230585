package com.example.Apoloplay.Model.repository;

import android.util.Base64;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;



import java.util.ArrayList;
import java.util.List;

import com.example.Apoloplay.Model.Music;
import com.example.Apoloplay.Model.auth.model.TokenResponse;
import com.example.Apoloplay.Model.auth.service.AuthTokenService;
import com.example.Apoloplay.Model.spotify.SpotifyResponse;
import com.example.Apoloplay.Model.spotify.SpotifyService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repositório: Responsável por gerir a lógica de dados do Spotify (Autenticação e Busca).
 */
public class SpotifyRepository {

    // --- CREDENCIAIS (ATENÇÃO: SUBSTITUIR!) ---
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String CLIENT_SECRET = "bf5f5e0affaa4a36985591b0a1e767ed";
    // ------------------------------------------

    private final SpotifyService spotifyService;
    private final AuthTokenService authTokenService;
    private String accessToken = null;

    // URLs Base do Spotify (Use HTTPS para evitar problemas de segurança de rede)
    private static final String TAG = "SpotifyRepo";
    // URL real para obter o Token
    private static final String AUTH_BASE_URL = "https://accounts.spotify.com/";
    private static final String API_BASE_URL = "https://api.spotify.com/v1/";
    public SpotifyRepository() {
        Log.d(TAG, "Inicializando Repositório e Serviços Retrofit.");

        // Inicializar Retrofit para Autenticação (Token)
        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        authTokenService = authRetrofit.create(AuthTokenService.class);

        // Inicializar Retrofit para a Web API (Busca)
        Retrofit apiRetrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spotifyService = apiRetrofit.create(SpotifyService.class);
    }

    /**
     * Inicia o processo de busca, garantindo que o token está disponível.
     */
    public void searchTracks(String query, MutableLiveData<List<Music>> resultLiveData) {
        if (accessToken != null) {
            Log.d(TAG, "Token já existe. A fazer busca por: " + query);
            performSearch(query, resultLiveData);
        } else {
            Log.d(TAG, "Token não encontrado. Iniciando autenticação...");
            fetchAccessToken(query, resultLiveData);
        }
    }

    /**
     * Passo 1: Faz a chamada POST para obter o Access Token (Client Credentials Flow).
     */
    private void fetchAccessToken(String query, MutableLiveData<List<Music>> resultLiveData) {
        String authHeaderString = CLIENT_ID + ":" + CLIENT_SECRET;

        // Codificação em Base64 para o cabeçalho "Authorization: Basic ..."
        String base64Credentials = Base64.encodeToString(authHeaderString.getBytes(), Base64.NO_WRAP);
        String authorizationHeader = "Basic " + base64Credentials;

        Log.d(TAG, "A enviar pedido de Token. URL: " + AUTH_BASE_URL + "api/token");

        authTokenService.getAccessToken(authorizationHeader, "client_credentials")
                .enqueue(new Callback<TokenResponse>() {
                    @Override
                    public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            accessToken = response.body().getAccessToken();
                            Log.d(TAG, "SUCESSO: Token obtido. A fazer busca...");
                            performSearch(query, resultLiveData);
                        } else {
                            // Código 400 ou 401: Falha na autenticação (geralmente credenciais erradas)
                            String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Erro desconhecido";
                            Log.e(TAG, "FALHA NO TOKEN! Código: " + response.code() + ", Erro: " + errorBody);
                            resultLiveData.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenResponse> call, Throwable t) {
                        // Falha de rede (sem internet, DNS falhou, URL errado)
                        Log.e(TAG, "FALHA DE REDE ao obter o Token: " + t.getMessage(), t);
                        resultLiveData.postValue(null);
                    }
                });
    }

    /**
     * Passo 2: Faz a chamada GET para o endpoint /search usando o Access Token.
     */
    private void performSearch(String query, MutableLiveData<List<Music>> resultLiveData) {
        String authorizationHeader = "Bearer " + accessToken;

        Log.d(TAG, "A enviar pedido de busca: " + API_BASE_URL + "search?q=" + query);

        spotifyService.searchTracks(
                authorizationHeader,
                query,
                "track", // Buscamos faixas
                20       // Limite de 20 resultados
        ).enqueue(new Callback<SpotifyResponse>() {
            @Override
            public void onResponse(Call<SpotifyResponse> call, Response<SpotifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Music> musicList = mapResponseToMusic(response.body());
                    Log.d(TAG, "SUCESSO: Busca concluída. Resultados mapeados: " + musicList.size());
                    resultLiveData.postValue(musicList);
                } else {
                    // 401 se o token tiver expirado, 400 se a query for má.
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Erro desconhecido";
                    Log.e(TAG, "FALHA NA BUSCA! Código: " + response.code() + ", Erro: " + errorBody);
                    resultLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<SpotifyResponse> call, Throwable t) {
                Log.e(TAG, "FALHA DE REDE na Busca: " + t.getMessage(), t);
                resultLiveData.postValue(null);
            }
        });
    }

    /**
     * Mapeia o objeto de resposta complexo do Spotify para o modelo Music simplificado.
     */
    private List<Music> mapResponseToMusic(SpotifyResponse response) {
        List<Music> musicList = new ArrayList<>();
        if (response.getTracks() != null && response.getTracks().getItems() != null) {
            for (SpotifyResponse.TrackItem item : response.getTracks().getItems()) {
                String title = item.getName();

                String artist = "Artista Desconhecido";
                if (item.getArtists() != null && !item.getArtists().isEmpty()) {
                    artist = item.getArtists().get(0).getName();
                }

                String imageUrl = null;
                if (item.getAlbum() != null && item.getAlbum().getImages() != null && !item.getAlbum().getImages().isEmpty()) {
                    // Pega a URL da primeira imagem (geralmente a de melhor qualidade)
                    imageUrl = item.getAlbum().getImages().get(0).getUrl();
                }

                musicList.add(new Music(title, artist, imageUrl));
            }
        }
        return musicList;
    }
}