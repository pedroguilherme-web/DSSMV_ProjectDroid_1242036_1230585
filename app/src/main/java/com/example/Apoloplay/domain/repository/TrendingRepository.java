package com.example.Apoloplay.domain.repository;

import android.util.Log;

import com.example.Apoloplay.data.RetrofitProvider;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.data.mapper.MusicMapper;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.domain.model.Music;

import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingRepository {

    private static final String TOP_50_GLOBAL_ID = "5ABHKGoOzxkaa28ttQV9sE";

    private final SpotifyService api = RetrofitProvider.provideSpotifyService();
    private final MusicMapper mapper = new MusicMapper();

    public void getTrending(Consumer<List<Music>> callback) {
        String token = ServiceLocator.sessionProvider().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            Log.e("TrendingRepository", "Sem token Spotify");
            callback.accept(List.of());
            return;
        } else
        {
            Log.d("TrendingRepository", "Token Spotify: " + token);
        }


        api.getPlaylistTracks("Bearer " + token, TOP_50_GLOBAL_ID, 100, 0)
                .enqueue(new Callback<PlaylistTracksResponseDTO>() {
                    @Override public void onResponse(Call<PlaylistTracksResponseDTO> call, Response<PlaylistTracksResponseDTO> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            callback.accept(mapper.fromPlaylistTracks(resp.body()));
                        } else {
                            Log.e("TrendingRepository", "Erro trending: " + resp.code());
                            callback.accept(List.of());
                        }
                    }

                    @Override public void onFailure(Call<PlaylistTracksResponseDTO> call, Throwable t) {
                        Log.e("TrendingRepository", "Falha trending: " + t.getMessage(), t);
                        callback.accept(List.of());
                    }
                });
    }
}
