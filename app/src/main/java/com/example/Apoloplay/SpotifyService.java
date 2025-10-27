package com.example.Apoloplay.data;

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyService {
    @GET("/v1/search")
    fun searchTracks(
            @Header("Authorization") authHeader: String,
            @Query("q") query: String,
            @Query("type") type: String = "track",
            @Query("limit") limit: Int = 10
    ): Call<SpotifyResponse>
}


data class SpotifyResponse(val tracks: Tracks)
data class Tracks(val items: List<Track>)
data class Track(val name: String, val artists: List<Artist>, val album: Album)
data class Artist(val name: String)
data class Album(val images: List<Image>)
data class Image(val url: String)
}