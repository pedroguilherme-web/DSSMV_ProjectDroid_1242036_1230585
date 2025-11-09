package com.example.Apoloplay.data.repository;

import android.util.Log;

import com.example.Apoloplay.data.auth.SessionProvider;
import com.example.Apoloplay.data.mapper.PlaylistMapper;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.AddTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.CreatePlaylistRequest;
import com.example.Apoloplay.data.remote.dto.PlaylistDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistsResponseDTO;
import com.example.Apoloplay.data.remote.dto.RemoveTracksRequest;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaylistsRepositoryImpl implements PlaylistsRepository {

    private static final String TAG = "PlaylistsRepo";
    public static final int PAGE_LIMIT = 100;

    private final SpotifyService api;
    private final SessionProvider session;
    private final List<Call<?>> inFlight = Collections.synchronizedList(new ArrayList<>());

    public PlaylistsRepositoryImpl(SpotifyService api, SessionProvider session) {
        this.api = api; this.session = session;
    }

    private String bearer() {
        String token = session.getUserAccessToken();
        if (token == null || token.isEmpty()) throw new IllegalStateException("Sem token de utilizador");
        return "Bearer " + token;
    }

    @Override
    public void getMyPlaylists(int limit, int offset, PlaylistsCallback cb) {
        Log.d(TAG, "getMyPlaylists: " + limit + "/" + offset);
        Call<PlaylistsResponseDTO> call = api.getMyPlaylists(bearer(), limit, offset);
        enqueueAndTrack(call, new Callback<PlaylistsResponseDTO>() {
            @Override public void onResponse(Call<PlaylistsResponseDTO> c, Response<PlaylistsResponseDTO> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(PlaylistMapper.toDomain(r.body()));
                else cb.onError(mapHttpToFriendly(r.code()));
            }
            @Override public void onFailure(Call<PlaylistsResponseDTO> c, Throwable t) { cb.onError(networkMsg(t)); }
        });
    }

    @Override
    public void getPlaylistTracks(String playlistId, int limit, int offset, TracksCallback cb) {
        Log.d(TAG, "getPlaylistTracks: " + playlistId + " " + limit + "/" + offset);
        Call<PlaylistTracksResponseDTO> call = api.getPlaylistTracks(bearer(), playlistId, limit, offset);
        enqueueAndTrack(call, new Callback<PlaylistTracksResponseDTO>() {
            @Override public void onResponse(Call<PlaylistTracksResponseDTO> c, Response<PlaylistTracksResponseDTO> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(PlaylistMapper.tracksToDomain(r.body()));
                else cb.onError(mapHttpToFriendly(r.code()));
            }
            @Override public void onFailure(Call<PlaylistTracksResponseDTO> c, Throwable t) { cb.onError(networkMsg(t)); }
        });
    }

    @Override
    public void createPlaylist(String name, String description, boolean isPublic, PlaylistCallback cb) {
        Call<SpotifyService.UserProfileDTO> meCall = api.getMe(bearer());
        enqueueAndTrack(meCall, new Callback<SpotifyService.UserProfileDTO>() {
            @Override public void onResponse(Call<SpotifyService.UserProfileDTO> c, Response<SpotifyService.UserProfileDTO> r) {
                if (!r.isSuccessful() || r.body()==null) { cb.onError(mapHttpToFriendly(r.code())); return; }
                String userId = r.body().id;
                CreatePlaylistRequest body = new CreatePlaylistRequest(name, description, isPublic);
                Call<PlaylistDTO> createCall = api.createPlaylist(bearer(), userId, body);
                enqueueAndTrack(createCall, new Callback<PlaylistDTO>() {
                    @Override public void onResponse(Call<PlaylistDTO> c2, Response<PlaylistDTO> r2) {
                        if (r2.isSuccessful() && r2.body()!=null) cb.onSuccess(PlaylistMapper.toDomain(r2.body()));
                        else cb.onError(mapHttpToFriendly(r2.code()));
                    }
                    @Override public void onFailure(Call<PlaylistDTO> c2, Throwable t) { cb.onError(networkMsg(t)); }
                });
            }
            @Override public void onFailure(Call<SpotifyService.UserProfileDTO> c, Throwable t) { cb.onError(networkMsg(t)); }
        });
    }

    @Override
    public void addTrackToPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb) {
        checkTrackExists(playlistId, singleTrackUri, new ExistsCallback() {
            @Override public void onResult(Boolean exists, String error) {
                if (error != null) { cb.onError(error); return; }
                if (Boolean.TRUE.equals(exists)) { cb.onError("Erro-Duplicado"); return; }
                Call<AddTracksResponseDTO> call = api.addTracks(bearer(), playlistId, singleTrackUri);
                enqueueAndTrack(call, new Callback<AddTracksResponseDTO>() {
                    @Override public void onResponse(Call<AddTracksResponseDTO> c, Response<AddTracksResponseDTO> r) {
                        if (r.isSuccessful()) cb.onSuccess(); else cb.onError(mapHttpToFriendly(r.code()));
                    }
                    @Override public void onFailure(Call<AddTracksResponseDTO> c, Throwable t) { cb.onError(networkMsg(t)); }
                });
            }
        });
    }

    @Override
    public void removeTrackFromPlaylist(String playlistId, String singleTrackUri, SimpleCallback cb) {
        Call<Void> call = api.removeTracks(bearer(), playlistId, new RemoveTracksRequest(singleTrackUri));
        enqueueAndTrack(call, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError(mapHttpToFriendly(r.code()));
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(networkMsg(t)); }
        });
    }

    @Override
    public void deletePlaylist(String playlistId, SimpleCallback cb) {
        Call<Void> call = api.deletePlaylist(bearer(), playlistId);
        enqueueAndTrack(call, new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.onSuccess(); else cb.onError(mapHttpToFriendly(r.code()));
            }
            @Override public void onFailure(Call<Void> c, Throwable t) { cb.onError(networkMsg(t)); }
        });
    }

    @Override
    public void cancelAll() {
        synchronized (inFlight) {
            for (Call<?> call : inFlight) try { if (call!=null && !call.isCanceled()) call.cancel(); } catch (Exception ignore) {}
            inFlight.clear();
        }
    }

    // ------- helpers -------
    private interface ExistsCallback { void onResult(Boolean exists, String error); }

    private void checkTrackExists(String playlistId, String targetUri, ExistsCallback cb) {
        scanPage(playlistId, targetUri, 0, PAGE_LIMIT, cb);
    }

    private void scanPage(String playlistId, String targetUri, int offset, int limit, ExistsCallback cb) {
        Call<PlaylistTracksResponseDTO> call = api.getPlaylistTracks(bearer(), playlistId, limit, offset);
        enqueueAndTrack(call, new Callback<PlaylistTracksResponseDTO>() {
            @Override public void onResponse(Call<PlaylistTracksResponseDTO> c, Response<PlaylistTracksResponseDTO> r) {
                if (!r.isSuccessful() || r.body()==null) { cb.onResult(null, mapHttpToFriendly(r.code())); return; }
                PlaylistTracksResponseDTO body = r.body();
                boolean found = false;
                if (body.items != null) {
                    for (PlaylistTrackItemDTO item : body.items) {
                        if (item != null && item.track != null && item.track.uri != null) {
                            if (item.track.uri.equalsIgnoreCase(targetUri)) { found = true; break; }
                        }
                    }
                }
                if (found) { cb.onResult(true, null); }
                else {
                    int pageCount = (body.items != null) ? body.items.size() : 0;
                    if (pageCount < limit) cb.onResult(false, null);
                    else scanPage(playlistId, targetUri, offset + limit, limit, cb);
                }
            }
            @Override public void onFailure(Call<PlaylistTracksResponseDTO> c, Throwable t) { cb.onResult(null, networkMsg(t)); }
        });
    }

    private <T> void enqueueAndTrack(Call<T> call, Callback<T> cb) {
        inFlight.add(call);
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) { inFlight.remove(call); cb.onResponse(c, r); }
            @Override public void onFailure(Call<T> c, Throwable t) { inFlight.remove(call); cb.onFailure(c, t); }
        });
    }

    private static String networkMsg(Throwable t) {
        return (t!=null && t.getMessage()!=null) ? t.getMessage() : "Falha de rede";
    }
    private static String mapHttpToFriendly(int code) {
        switch (code) {
            case 401: return "Sessão expirada. Inicia sessão novamente.";
            case 403: return "Sem permissões para esta ação.";
            case 429: return "Muitos pedidos. Tenta mais tarde.";
            case 500: case 502: case 503: case 504: return "Servidor indisponível. Tenta novamente.";
            default: return "Erro ("+code+").";
        }
    }
}
