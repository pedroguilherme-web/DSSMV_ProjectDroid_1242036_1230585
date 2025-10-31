package com.example.Apoloplay.View;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.Model.Music;
import com.example.Apoloplay.Model.MusicAdapter;
import com.example.Apoloplay.R;
import com.example.Apoloplay.ViewModel.MusicViewModel;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MusicViewModel viewModel;
    private MusicAdapter musicAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // O seu layout XML

        // 1. Inicializar UI components (Usando os seus IDs: searchInput e recyclerView)
        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);

        // ** Componentes removidos: loadingBar e searchButton não são inicializados **

        // 2. Configurar o RecyclerView
        musicAdapter = new MusicAdapter(new ArrayList<Music>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(musicAdapter);

        // 3. Inicializar o ViewModel
        viewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // 4. Configurar a acção ENTER do teclado (mais fácil)
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String query = searchInput.getText().toString();
                if (!query.trim().isEmpty()) {
                    viewModel.search(query); // Dispara a busca
                } else {
                    Toast.makeText(this, "Termo de busca vazio.", Toast.LENGTH_SHORT).show();
                }
                return true; // Consome o evento
            }
            return false;
        });

        // 5. Observar os dados (o coração do MVVM)
        viewModel.getMusicResults().observe(this, musicList -> {
            // Fim do carregamento. Apenas ativamos o input novamente.
            searchInput.setEnabled(true);

            if (musicList != null && !musicList.isEmpty()) {
                musicAdapter.setMusicList(musicList);
                musicAdapter.notifyDataSetChanged();
                Toast.makeText(this, musicList.size() + " resultados encontrados.", Toast.LENGTH_SHORT).show();
            } else {
                musicAdapter.setMusicList(new ArrayList<>());
                musicAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Nenhuma música encontrada.", Toast.LENGTH_LONG).show();
            }
        });

        // 6. Observar o estado de loading (apenas para desativar o input)
        viewModel.getIsLoading().observe(this, isLoading -> {
            // Desativa o input enquanto a busca está a decorrer
            searchInput.setEnabled(!isLoading);
            if (isLoading) {
                Toast.makeText(this, "A procurar...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}