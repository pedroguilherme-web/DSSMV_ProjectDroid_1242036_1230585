package com.example.Apoloplay.Model.repository;

import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.Apoloplay.Model.*;
import com.example.Apoloplay.Model.auth.model.TokenResponse;
import com.example.Apoloplay.Model.auth.service.AuthTokenService;
import com.example.Apoloplay.Model.spotify.SpotifyMapper;
import com.example.Apoloplay.Model.spotify.SpotifyResponse;
import com.example.Apoloplay.Model.spotify.SpotifyService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotifyRepository {

    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String CLIENT_SECRET = "bf5f5e0affaa4a36985591b0a1e767ed";

    private static final String TAG = "SpotifyRepo";
    private static final String AUTH_BASE_URL = "https://accounts.spotify.com/";
    private static final String API_BASE_URL  = "https://api.spotify.com/v1/";

    private final SpotifyService spotifyService;
    private final AuthTokenService authTokenService;

    /** token pode ser de app (client_credentials) ou de utilizador (implicit flow) */
    private String accessToken = null;

    public SpotifyRepository() {
        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        authTokenService = authRetrofit.create(AuthTokenService.class);

        Retrofit apiRetrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spotifyService = apiRetrofit.create(SpotifyService.class);
    }

    // ========= Search (usa client_credentials se preciso) =========
    public void searchTracks(String query, MutableLiveData<List<Music>> resultLiveData) {
        if (accessToken != null) {
            performSearch(query, resultLiveData);
        } else {
            fetchAccessToken(query, resultLiveData);
        }
    }

    private void fetchAccessToken(String query, MutableLiveData<List<Music>> resultLiveData) {
        String creds = CLIENT_ID + ":" + CLIENT_SECRET;
        String b64   = Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        String auth  = "Basic " + b64;

        authTokenService.getAccessToken(auth, "client_credentials")
                .enqueue(new Callback<TokenResponse>() {
                    @Override public void onResponse(Call<TokenResponse> c, Response<TokenResponse> r) {
                        if (r.isSuccessful() && r.body()!=null) {
                            accessToken = r.body().getAccessToken();
                            performSearch(query, resultLiveData);
                        } else {
                            Log.e(TAG, "Falha token app: " + r.code());
                            resultLiveData.postValue(null);
                        }
                    }
                    @Override public void onFailure(Call<TokenResponse> c, Throwable t) {
                        Log.e(TAG, "Rede token app", t);
                        resultLiveData.postValue(null);
                    }
                });
    }

    private void performSearch(String query, MutableLiveData<List<Music>> resultLiveData) {
        spotifyService.searchTracks(bearer(), query, "track", 20)
                .enqueue(new Callback<SpotifyResponse>() {
                    @Override public void onResponse(Call<SpotifyResponse> c, Response<SpotifyResponse> r) {
                        if (r.isSuccessful() && r.body()!=null) {
                            resultLiveData.postValue(SpotifyMapper.mapResponseToMusic(r.body()));
                        } else {
                            Log.e(TAG, "Falha busca: " + r.code());
                            resultLiveData.postValue(null);
                        }
                    }
                    @Override public void onFailure(Call<SpotifyResponse> c, Throwable t) {
                        Log.e(TAG, "Rede busca", t);
                        resultLiveData.postValue(null);
                    }
                });
    }

    // ========= Token do utilizador =========
    public void setAccessToken(String token) {
        this.accessToken = token;
        Log.d(TAG, "Access token definido no repository.");
    }

    public String getAccessToken() { return accessToken; }

    private String bearer() {
        if (accessToken == null || accessToken.isEmpty())
            throw new IllegalStateException("Access Token não definido");
        return "Bearer " + accessToken;
    }

    // ========= Wrappers REST p/ playlists (usar quando ativares o fluxo) =========

    public Call<com.example.Apoloplay.Model.UserProfile> getMe() {
        return spotifyService.getMe(bearer());
    }
    public Call<PlaylistsResponse> getMyPlaylists(int limit) {
        return spotifyService.getMyPlaylists(bearer(), limit);
    }

    public Call<Playlist> createPlaylist(String userId, CreatePlaylistRequest body) {
        return spotifyService.createPlaylist(bearer(), userId, body);
    }

    /** urisCsv: "spotify:track:ID1,spotify:track:ID2" */
    public Call<AddTracksResponse> addTracks(String playlistId, String urisCsv) {
        return spotifyService.addTracks(bearer(), playlistId, urisCsv);
    }

    public Call<Void> removeTrack(String playlistId, String singleTrackUri) {
        return spotifyService.removeTracks(bearer(), playlistId, new RemoveTracksRequest(singleTrackUri));
    }
}
