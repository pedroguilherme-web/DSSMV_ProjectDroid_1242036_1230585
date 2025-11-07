package com.example.Apoloplay.Model.spotify;



import com.example.Apoloplay.Model.*;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import retrofit2.Call;
import retrofit2.http.*;

public interface SpotifyService {

    // ---- SEARCH (podes usar token de app) ----
    @GET("search")
    Call<SpotifyResponse> searchTracks(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );

    // ---- USER-CONTEXT (exigem token de UTILIZADOR) ----

    @GET("playlists/{playlist_id}/tracks")
    Call<PlaylistTracksResponseDTO> getPlaylistTracks(
            @Header("Authorization") String bearer,
            @Path("playlist_id") String playlistId,
            @Query("limit") int limit,
            @Query("offset") int offset
    );
    @GET("me/playlists")
    Call<PlaylistsResponse> getMyPlaylists(
            @Header("Authorization") String authorization,
            @Query("limit") int limit
    );

    @POST("users/{user_id}/playlists")
    Call<Playlist> createPlaylist(
            @Header("Authorization") String authorization,
            @Path("user_id") String userId,
            @Body CreatePlaylistRequest body
    );

    // urisCsv: "spotify:track:AAA,spotify:track:BBB"
    @POST("playlists/{playlist_id}/tracks")
    Call<AddTracksResponse> addTracks(
            @Header("Authorization") String authorization,
            @Path("playlist_id") String playlistId,
            @Query("uris") String urisCsv
    );

    @HTTP(method = "DELETE", path = "playlists/{playlist_id}/tracks", hasBody = true)
    Call<Void> removeTracks(
            @Header("Authorization") String authorization,
            @Path("playlist_id") String playlistId,
            @Body RemoveTracksRequest body
    );











    @GET("me")
    Call<com.example.Apoloplay.Model.UserProfile> getMe(
            @Header("Authorization") String bearer
    );



}
