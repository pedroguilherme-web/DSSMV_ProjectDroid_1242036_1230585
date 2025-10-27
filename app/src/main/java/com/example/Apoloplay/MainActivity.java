package com.example.Apoloplay.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.Apoloplay.R
import com.example.Apoloplay.data.SpotifyService
import com.example.Apoloplay.models.Music
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchSpotifyTracks("dua lipa") // exemplo
    }

    private fun fetchSpotifyTracks(query: String) {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(SpotifyService::class.java)

        val token = "Bearer TEU_TOKEN_AQUI"
        val call = service.searchTracks(token, query)

        call.enqueue(object : Callback<com.pedroguilherme_web.dssmv_projectdroid.data.SpotifyResponse> {
            override fun onResponse(
                    call: Call<com.example.Apoloplay.data.SpotifyResponse>,
            response: Response<com.example.Apoloplay.data.SpotifyResponse>
            ) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks?.items ?: emptyList()
                    val musicList = tracks.map {
                        val imageUrl = it.album.images.firstOrNull()?.url ?: ""
                        Music(it.name, it.artists.firstOrNull()?.name ?: "Desconhecido", imageUrl)
                    }
                    recyclerView.adapter = MusicAdapter(musicList)
                }
            }

            override fun onFailure(call: Call<com.example.Apoloplay.data.SpotifyResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}