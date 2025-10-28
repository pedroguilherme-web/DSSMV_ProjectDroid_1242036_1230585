package com.example.Apoloplay;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log; // Adicionado para logging

// 🎯 Imports essenciais e corrigidos
import com.example.Apoloplay.data.SpotifyService; // Importação correta da interface
import com.example.Apoloplay.models.Music;
import com.example.Apoloplay.models.SpotifyResponse; // Novo import necessário

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Garanta que este layout existe

        recyclerView = findViewById(R.id.recyclerView); // Garanta que este ID existe em activity_main.xml
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchSpotifyTracks("dua lipa"); // Exemplo de busca
    }

    private void fetchSpotifyTracks(String query) {
        // Base URL do Spotify Web API (Geralmente terminando com /v1, mas mantendo o que você sugeriu com correção de erro)
        // ⚠️ Nota: A URL correta da API do Spotify é https://api.spotify.com/v1.
        // Se estiver usando um mock, ajuste a URL conforme a documentação do mock.
        final String SPOTIFY_BASE_URL = "https://api.spotify.com/v1/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SPOTIFY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyService service = retrofit.create(SpotifyService.class);

        // ⚠️ ALERTA: Substitua "TEU_TOKEN_AQUI" por um token Bearer válido e atual.
        // Tokens do Spotify são de curta duração (expiram a cada hora).
        String token = "Bearer TEU_TOKEN_AQUI";

        // 🛠️ Chamada ao serviço CORRIGIDA: Incluindo 'type' e 'limit'
        String type = "track";
        int limit = 20;

        Call<SpotifyResponse> call = service.searchTracks(token, query, type, limit);

        call.enqueue(new Callback<SpotifyResponse>() {
            @Override
            public void onResponse(Call<SpotifyResponse> call, Response<SpotifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Acesso ao corpo da resposta agora é possível devido ao import de SpotifyResponse
                    List<SpotifyResponse.TrackItem> tracks = response.body().getTracks().getItems();
                    List<Music> musicList = new ArrayList<>();

                    for (SpotifyResponse.TrackItem track : tracks) {
                        String imageUrl = "";
                        // Lógica de extração segura da URL da imagem
                        if (track.getAlbum() != null && track.getAlbum().getImages() != null && !track.getAlbum().getImages().isEmpty()) {
                            imageUrl = track.getAlbum().getImages().get(0).getUrl();
                        }

                        String artistName = "Desconhecido";
                        // Lógica de extração segura do nome do artista
                        if (track.getArtists() != null && !track.getArtists().isEmpty()) {
                            artistName = track.getArtists().get(0).getName();
                        }

                        musicList.add(new Music(track.getName(), artistName, imageUrl));
                    }

                    // Define o adaptador (MusicAdapter deve ter sido criado)
                    recyclerView.setAdapter(new MusicAdapter(musicList));

                } else {
                    // Loga o erro de resposta da API (e.g., Token inválido, Status 401)
                    Log.e(TAG, "Erro na resposta da API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SpotifyResponse> call, Throwable t) {
                // Loga a falha de rede ou de conversão
                Log.e(TAG, "Falha na chamada Retrofit. Verifique a conexão ou a URL.", t);
            }
        });
    }
}