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

    // --------- DUP CHECK + ADD ---------
    @Override
    public void addTrackToPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb) {
        // 1) Verificar se já existe (paginação 100/100)
        checkTrackExists(playlistId, singleTrackUri, new ExistsCallback() {
            @Override public void onResult(Boolean exists, String error) {
                if (error != null) {
                    cb.onError(error);
                } else if (Boolean.TRUE.equals(exists)) {
                    cb.onError("Erro-Duplicado");
                } else {
                    // 2) Não existe → adicionar
                    api.addTracks(bearer(), playlistId, singleTrackUri)
                            .enqueue(new Callback<AddTracksResponseDTO>() {
                                @Override public void onResponse(Call<AddTracksResponseDTO> c, Response<AddTracksResponseDTO> r) {
                                    if (r.isSuccessful()) cb.onSuccess(); else cb.onError("HTTP " + r.code());
                                }
                                @Override public void onFailure(Call<AddTracksResponseDTO> c, Throwable t) {
                                    cb.onError(msg(t));
                                }
                            });
                }
            }
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

    @Override
    public void deletePlaylist(String playlistId, SimpleCallback cb) {
        api.deletePlaylist(bearer(), playlistId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError("HTTP " + r.code());
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(msg(t)); }
        });
    }

    // ---------- Helpers privados ----------

    private interface ExistsCallback { void onResult(Boolean exists, String error); }

    private void checkTrackExists(String playlistId, String targetUri, ExistsCallback cb) {
        final int LIMIT = 100;
        scanPage(playlistId, targetUri, 0, LIMIT, cb);
    }

    private void scanPage(String playlistId, String targetUri, int offset, int limit, ExistsCallback cb) {
        api.getPlaylistTracks(bearer(), playlistId, limit, offset)
                .enqueue(new Callback<PlaylistTracksResponseDTO>() {
                    @Override public void onResponse(Call<PlaylistTracksResponseDTO> c, Response<PlaylistTracksResponseDTO> r) {
                        if (!r.isSuccessful() || r.body()==null) {
                            cb.onResult(null, "HTTP " + r.code());
                            return;
                        }
                        PlaylistTracksResponseDTO body = r.body();

                        boolean found = false;
                        if (body.items != null) {
                            for (var item : body.items) {
                                if (item != null && item.track != null && item.track.uri != null) {
                                    if (item.track.uri.equalsIgnoreCase(targetUri)) {
                                        found = true; break;
                                    }
                                }
                            }
                        }

                        if (found) {
                            cb.onResult(true, null);
                        } else {
                            int pageCount = (body.items != null) ? body.items.size() : 0;
                            if (pageCount < limit) {
                                // última página → não existe
                                cb.onResult(false, null);
                            } else {
                                // continuar a paginar
                                scanPage(playlistId, targetUri, offset + limit, limit, cb);
                            }
                        }
                    }
                    @Override public void onFailure(Call<PlaylistTracksResponseDTO> c, Throwable t) {
                        cb.onResult(null, msg(t));
                    }
                });
    }

    private static String msg(Throwable t){
        return (t!=null && t.getMessage()!=null) ? t.getMessage() : "Falha de rede";
    }
}
