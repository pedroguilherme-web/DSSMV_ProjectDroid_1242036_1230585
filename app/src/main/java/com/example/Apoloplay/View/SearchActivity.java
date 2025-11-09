package com.example.Apoloplay.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.search.SearchRowAdapter;
import com.example.Apoloplay.ui.search.SearchUiState;
import com.example.Apoloplay.ui.search.SearchViewModel;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SearchActivity extends AppCompatActivity {

    // --- Spotify OAuth (iguais à Main) ---
    private static final int REQ_CODE_SPOTIFY = 2342;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

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

        // ENTER → pesquisar
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP);
            if (enter) {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) vm.search(q);
                return true;
            }
            return false;
        });

        // Query inicial vinda da MainActivity → pesquisa automática
        String initial = getIntent().getStringExtra(MainActivity.EXTRA_INITIAL_QUERY);
        if (initial != null && !initial.isEmpty()) {
            searchInput.setText(initial);
            searchInput.setSelection(initial.length());
            vm.search(initial);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureLoggedIn();
    }

    // --- UI render com enum + switch ---
    private void render(SearchUiState s) {
        switch (s.getStatus()) {
            case IDLE:

                adapter.submit(java.util.Collections.emptyList());
                break;

            case LOADING:

                break;

            case DATA:
                adapter.submit(s.getResults());

                break;

            case ERROR:
                adapter.submit(java.util.Collections.emptyList());
                if (s.getError() != null) {
                    Toast.makeText(this, s.getError(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // --- Navegação ---
    private void openDetails(Music m) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra("MUSIC_DETAILS", m);
        startActivity(i);
    }

    // ---------- Login Spotify (só token) ----------
    private void ensureLoggedIn() {
        String token = ServiceLocator.sessionProvider().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            startSpotifyLogin();
        }
    }

    private void startSpotifyLogin() {
        AuthorizationRequest request = new AuthorizationRequest.Builder(
                CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
        )
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-modify-public",
                        "playlist-modify-private",
                        "user-read-email",
                        "user-read-private"
                })
                .build();

        AuthorizationClient.openLoginActivity(this, REQ_CODE_SPOTIFY, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_CODE_SPOTIFY) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                ServiceLocator.sessionProvider().setUserAccessToken(response.getAccessToken());
            } else if (response.getType() == AuthorizationResponse.Type.ERROR) {
                Toast.makeText(this, "Erro ao iniciar sessão: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
