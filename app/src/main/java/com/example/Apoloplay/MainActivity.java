package com.example.Apoloplay.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.SpotifyService;
import com.example.Apoloplay.models.Music;
import com.example.Apoloplay.MusicAdapter;
import com.example.Apoloplay.data.SpotifyResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchSpotifyTracks("dua lipa"); // exemplo
    }

    private void fetchSpotifyTracks(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyService service = retrofit.create(SpotifyService.class);

        String token = "Bearer TEU_TOKEN_AQUI";
        Call<SpotifyResponse> call = service.searchTracks(token, query);

        call.enqueue(new Callback<SpotifyResponse>() {
            @Override
            public void onResponse(Call<SpotifyResponse> call, Response<SpotifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SpotifyResponse.TrackItem> tracks = response.body().getTracks().getItems();
                    List<Music> musicList = new ArrayList<>();

                    for (SpotifyResponse.TrackItem track : tracks) {
                        String imageUrl = "";
                        if (track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
                            imageUrl = track.getAlbum().getImages().get(0).getUrl();
                        }

                        String artistName = "Desconhecido";
                        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
                            artistName = track.getArtists().get(0).getName();
                        }

                        musicList.add(new Music(track.getName(), artistName, imageUrl));
                    }

                    recyclerView.setAdapter(new MusicAdapter(musicList));
                }
            }

            @Override
            public void onFailure(Call<SpotifyResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}