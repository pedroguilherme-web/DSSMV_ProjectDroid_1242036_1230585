package com.example.Apoloplay.data.repository;

import com.example.Apoloplay.data.auth.SessionProvider;
import com.example.Apoloplay.data.mapper.PlaylistMapper;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.AddTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.CreatePlaylistRequest;
import com.example.Apoloplay.data.remote.dto.PlaylistDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistsResponseDTO;
import com.example.Apoloplay.data.remote.dto.RemoveTracksRequest;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistsRepositoryImpl implements PlaylistsRepository {

    private final SpotifyService api;
    private final SessionProvider session;

    public PlaylistsRepositoryImpl(SpotifyService api, SessionProvider session) {
        this.api = api;
        this.session = session;
    }

    private String bearer() {
        String token = session.getUserAccessToken();
        if (token == null || token.isEmpty()) throw new IllegalStateException("Sem token de utilizador");
        return "Bearer " + token;
    }

    @Override
    public void getMyPlaylists(int limit, int offset, PlaylistsCallback cb) {
        api.getMyPlaylists(bearer(), limit, offset).enqueue(new Callback<PlaylistsResponseDTO>() {
            @Override public void onResponse(Call<PlaylistsResponseDTO> c, Response<PlaylistsResponseDTO> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(PlaylistMapper.toDomain(r.body()));
                else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<PlaylistsResponseDTO> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    @Override
    public void getPlaylistTracks(String playlistId, int limit, int offset, TracksCallback cb) {
        api.getPlaylistTracks(bearer(), playlistId, limit, offset).enqueue(new Callback<PlaylistTracksResponseDTO>() {
            @Override public void onResponse(Call<PlaylistTracksResponseDTO> c, Response<PlaylistTracksResponseDTO> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(PlaylistMapper.tracksToDomain(r.body()));
                else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<PlaylistTracksResponseDTO> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    @Override
    public void createPlaylist(String name, String description, boolean isPublic, PlaylistCallback cb) {
        api.getMe(bearer()).enqueue(new Callback<SpotifyService.UserProfileDTO>() {
            @Override public void onResponse(Call<SpotifyService.UserProfileDTO> c, Response<SpotifyService.UserProfileDTO> r) {
                if (!r.isSuccessful() || r.body()==null) { cb.onError("HTTP " + r.code()); return; }
                String userId = r.body().id;
                CreatePlaylistRequest body = new CreatePlaylistRequest(name, description, isPublic);
                api.createPlaylist(bearer(), userId, body).enqueue(new Callback<PlaylistDTO>() {
                    @Override public void onResponse(Call<PlaylistDTO> c2, Response<PlaylistDTO> r2) {
                        if (r2.isSuccessful() && r2.body()!=null) cb.onSuccess(PlaylistMapper.toDomain(r2.body()));
                        else cb.onError("HTTP " + r2.code());
                    }
                    @Override public void onFailure(Call<PlaylistDTO> c2, Throwable t) { cb.onError(msg(t)); }
                });
            }
            @Override public void onFailure(Call<SpotifyService.UserProfileDTO> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    @Override
    public void addTrackToPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb) {
        api.addTracks(bearer(), playlistId, singleTrackUri).enqueue(new Callback<AddTracksResponseDTO>() {
            @Override public void onResponse(Call<AddTracksResponseDTO> c, Response<AddTracksResponseDTO> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<AddTracksResponseDTO> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    @Override
    public void removeTrackFromPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb) {
        api.removeTracks(bearer(), playlistId, new RemoveTracksRequest(singleTrackUri)).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    // NOVO:
    @Override
    public void deletePlaylist(String playlistId, SimpleCallback cb) {
        api.deletePlaylist(bearer(), playlistId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    private static String msg(Throwable t){ return (t!=null && t.getMessage()!=null) ? t.getMessage() : "Falha de rede"; }
}
