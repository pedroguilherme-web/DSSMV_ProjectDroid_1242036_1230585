package com.example.Apoloplay.data.repository;

import android.util.Base64;
import android.util.Log;

import com.example.Apoloplay.data.RetrofitProvider;
import com.example.Apoloplay.data.mapper.MusicMapper;
import com.example.Apoloplay.data.remote.AuthTokenService;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.TokenResponseDTO;
import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.SearchRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRepositoryImpl implements SearchRepository {

 
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String CLIENT_SECRET = "bf5f5e0affaa4a36985591b0a1e767ed";

    private final AuthTokenService auth;
    private final SpotifyService api;

    private String appToken; // token de app (client_credentials) em cache

    public SearchRepositoryImpl() {
        this.auth = RetrofitProvider.provideAuthRetrofit().create(AuthTokenService.class);
        this.api  = RetrofitProvider.provideApiRetrofit().create(SpotifyService.class);
    }

    @Override
    public void searchTracks(String query, Callback cb) {
        if (appToken == null || appToken.isEmpty()) {
            fetchAppTokenAndSearch(query, cb);
        } else {
            performSearch(query, cb);
        }
    }

    private void fetchAppTokenAndSearch(String query, Callback cb){
        String creds = CLIENT_ID + ":" + CLIENT_SECRET;
        String b64   = Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        String authH = "Basic " + b64;

        auth.getAccessToken(authH, "client_credentials")
                .enqueue(new retrofit2.Callback<TokenResponseDTO>() {
                    @Override public void onResponse(Call<TokenResponseDTO> call, Response<TokenResponseDTO> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            appToken = resp.body().accessToken;
                            performSearch(query, cb);
                        } else {
                            cb.onError("Falha token app: HTTP " + resp.code());
                        }
                    }
                    @Override public void onFailure(Call<TokenResponseDTO> call, Throwable t) {
                        cb.onError(t != null && t.getMessage()!=null ? t.getMessage() : "Erro rede token");
                    }
                });
    }

    private void performSearch(String query, Callback cb){
        api.searchTracks("Bearer " + appToken, query, "track", 30)
                .enqueue(new retrofit2.Callback<SpotifySearchResponseDTO>() {
                    @Override public void onResponse(Call<SpotifySearchResponseDTO> call, Response<SpotifySearchResponseDTO> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            List<Music> list = MusicMapper.toDomain(resp.body());
                            cb.onSuccess(list);
                        } else {
                            cb.onError("Falha busca: HTTP " + resp.code());
                        }
                    }
                    @Override public void onFailure(Call<SpotifySearchResponseDTO> call, Throwable t) {
                        cb.onError(t != null && t.getMessage()!=null ? t.getMessage() : "Erro rede busca");
                    }
                });
    }
}
