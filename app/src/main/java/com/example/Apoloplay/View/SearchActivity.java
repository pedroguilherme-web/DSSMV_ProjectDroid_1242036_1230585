// SearchActivity.java
package com.example.Apoloplay.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.search.SearchRowAdapter;
import com.example.Apoloplay.ui.search.SearchUiState;
import com.example.Apoloplay.ui.search.SearchViewModel;

public class SearchActivity extends AppCompatActivity {

    private SearchViewModel vm;
    private SearchRowAdapter adapter;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView rv = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);

        adapter = new SearchRowAdapter(this::openDetails);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(SearchViewModel.class);
        vm.getState().observe(this, this::render);

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) vm.search(q);
                return true;
            }
            return false;
        });
    }

    private void render(SearchUiState s){
        // poderias mostrar/progress aqui (desabilitar input, etc.)
        adapter.submit(s.items);
        if (s.error != null) Toast.makeText(this, s.error, Toast.LENGTH_SHORT).show();
    }

    private void openDetails(Music m){
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra("MUSIC_DETAILS", m); // DOMAIN model
        startActivity(i);
    }
}
