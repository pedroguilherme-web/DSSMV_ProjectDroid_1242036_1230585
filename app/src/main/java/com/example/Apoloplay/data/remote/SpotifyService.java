package com.example.Apoloplay.data.remote;

import com.example.Apoloplay.data.remote.dto.AddTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.CreatePlaylistRequest;
import com.example.Apoloplay.data.remote.dto.PlaylistDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistsResponseDTO;
import com.example.Apoloplay.data.remote.dto.RemoveTracksRequest;

import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import retrofit2.Call;
import retrofit2.http.*;

public interface SpotifyService {

    @GET("me")
    Call<UserProfileDTO> getMe(@Header("Authorization") String bearer);

    @GET("me/playlists")
    Call<PlaylistsResponseDTO> getMyPlaylists(
            @Header("Authorization") String bearer,
            @Query("limit") int limit,
            @Query("offset") int offset
    );


    @POST("users/{user_id}/playlists")
    Call<PlaylistDTO> createPlaylist(
            @Header("Authorization") String bearer,
            @Path("user_id") String userId,
            @Body CreatePlaylistRequest body
    );

    @POST("playlists/{playlist_id}/tracks")
    Call<AddTracksResponseDTO> addTracks(
            @Header("Authorization") String bearer,
            @Path("playlist_id") String playlistId,
            @Query("uris") String urisCsv
    );

    @HTTP(method = "DELETE", path = "playlists/{playlist_id}/tracks", hasBody = true)
    Call<Void> removeTracks(
            @Header("Authorization") String bearer,
            @Path("playlist_id") String playlistId,
            @Body RemoveTracksRequest body
    );

    @DELETE("playlists/{playlist_id}/followers")
    Call<Void> deletePlaylist(
            @Header("Authorization") String bearer,
            @Path("playlist_id") String playlistId
    );


    @GET("playlists/{playlist_id}/tracks")
    Call<PlaylistTracksResponseDTO> getPlaylistTracks(
            @Header("Authorization") String bearer,
            @Path("playlist_id") String playlistId,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("search")
    Call<SpotifySearchResponseDTO> searchTracks(
            @Header("Authorization") String bearer,
            @Query("q") String query,
            @Query("type") String type,
            @Query("limit") int limit
    );

    // DTO mínimo do /me
    class UserProfileDTO {
        public String id;
        public String display_name;
    }
}
