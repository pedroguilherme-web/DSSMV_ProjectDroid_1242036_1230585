package com.example.Apoloplay.data.repository.implementation;



import android.util.Log;

import com.example.Apoloplay.data.auth.SessionProvider;
import com.example.Apoloplay.data.mapper.MusicMapper;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.repository.interfaces.TrendingRepository;
import com.example.Apoloplay.data.model.Music;


import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementação concreta do TrendingRepository que vai ao Spotify buscar o Top 50 Global.
 */
public class TrendingRepositoryImpl implements TrendingRepository {

    private static final String TAG = "TrendingRepository";
    private static final String TOP_100_GLOBAL_ID = "5ABHKGoOzxkaa28ttQV9sE";

    private final SpotifyService api;
    private final SessionProvider session;
    private final MusicMapper mapper;

    public TrendingRepositoryImpl(SpotifyService api,
                                  SessionProvider session,
                                  MusicMapper mapper) {
        this.api = api;
        this.session = session;
        this.mapper = mapper;
    }

    private String bearer() {
        String token = session.getUserAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Sem token Spotify");
        }
        return "Bearer " + token;
    }

    @Override
    public void getTrending(Consumer<List<Music>> callback) {

        String token = session.getUserAccessToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Sem token Spotify");
            callback.accept(Collections.emptyList());
            return;
        } else {
            Log.d(TAG, "Token Spotify: " + token);
        }

        api.getPlaylistTracks(bearer(), TOP_100_GLOBAL_ID, 100, 0)
                .enqueue(new Callback<PlaylistTracksResponseDTO>() {

                    @Override
                    public void onResponse(Call<PlaylistTracksResponseDTO> call,
                                           Response<PlaylistTracksResponseDTO> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {

                            List<Music> result = mapper.fromPlaylistTracks(resp.body());
                            callback.accept(result);

                        } else {
                            Log.e(TAG, "Erro trending: HTTP " + resp.code());
                            callback.accept(Collections.emptyList());
                        }
                    }

                    @Override
                    public void onFailure(Call<PlaylistTracksResponseDTO> call, Throwable t) {
                        Log.e(TAG, "Falha trending: " + t.getMessage(), t);
                        callback.accept(Collections.emptyList());
                    }
                });
    }
}
