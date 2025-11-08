package com.example.Apoloplay.View;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1337;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";
    public static final String EXTRA_INITIAL_QUERY = "EXTRA_INITIAL_QUERY";

    private PlayerViewModel playerVm;

    private View root;
    private View searchBar;
    private EditText searchInput;
    private ImageButton searchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        playerVm = new ViewModelProvider(this).get(PlayerViewModel.class);

        root = findViewById(R.id.root);
        searchBar = findViewById(R.id.search_bar_container);
        searchInput = findViewById(R.id.searchInput);
        searchIcon = findViewById(R.id.btn_search_icon);

        // Enter → abre SearchActivity
        searchInput.setOnEditorActionListener((v, actionId, ev) -> {
            boolean enter = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER && ev.getAction() == KeyEvent.ACTION_UP);
            if (enter) {
                String q = searchInput.getText().toString().trim();
                if (!q.isEmpty()) openSearchWithQuery(q);
                return true;
            }
            return false;
        });

        // Clique no ícone da lupa
        searchIcon.setOnClickListener(v -> {
            String q = searchInput.getText().toString().trim();
            if (!q.isEmpty()) openSearchWithQuery(q);
        });

        // Botão “olho” abre Playlists
        ImageButton btnOpenPlaylists = findViewById(R.id.btn_open_playlists);
        btnOpenPlaylists.setOnClickListener(v -> ensureLoginThen(() -> {
            playerVm.connect(this);
            goToPlaylists();
        }));

        installKeyboardVisibilityListener();
    }

    private void openSearchWithQuery(String q) {
        Intent i = new Intent(this, SearchActivity.class);
        i.putExtra(EXTRA_INITIAL_QUERY, q);
        startActivity(i);
    }

    private void ensureLoginThen(Runnable afterLogin) {
        String token = ServiceLocator.sessionProvider().getUserAccessToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Inicia sessão no Spotify primeiro.", Toast.LENGTH_SHORT).show();
            startSpotifyLogin();
        } else {
            afterLogin.run();
        }
    }

    private void startSpotifyLogin() {
        AuthorizationRequest request = new AuthorizationRequest.Builder(
                CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI
        )
                .setScopes(new String[]{
                        "playlist-read-private",
                        "playlist-modify-public",
                        "playlist-modify-private",
                        "user-read-email",
                        "user-read-private"
                })
                .build();

        AuthorizationClient.openLoginActivity(this, REQ_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                ServiceLocator.sessionProvider().setUserAccessToken(response.getAccessToken());
                playerVm.connect(this);
                Toast.makeText(this, "Sessão iniciada com sucesso.", Toast.LENGTH_SHORT).show();
                goToPlaylists();
            } else if (response.getType() == AuthorizationResponse.Type.ERROR) {
                Toast.makeText(this, "Erro ao iniciar sessão: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToPlaylists() {
        startActivity(new Intent(this, PlaylistsActivity.class));
    }

    // --- teclado: faz a barra subir para cima do teclado ---


    private void installKeyboardVisibilityListener() {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean keyboardVisible = false;

            @Override public void onGlobalLayout() {
                Rect r = new Rect();
                root.getWindowVisibleDisplayFrame(r); // área visível (sem teclado)
                int screenHeight = root.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                boolean nowVisible = keypadHeight > screenHeight * 0.15;
                if (nowVisible != keyboardVisible) {
                    keyboardVisible = nowVisible;
                }
                // recalcula sempre que muda o layout (teclado subiu/desceu)
                adjustSearchBarForVisibleFrame(r);
            }
        });
    }

    private void adjustSearchBarForVisibleFrame(Rect visibleFrame) {
        // posição atual da barra no ecrã
        int[] loc = new int[2];
        searchBar.getLocationOnScreen(loc);
        int barBottomOnScreen = loc[1] + searchBar.getHeight();

        int marginPx = dp(12); // margem desejada acima do teclado
        int overlap = (barBottomOnScreen + marginPx) - visibleFrame.bottom;

        if (overlap > 0) {
            // sobe só o necessário (negativo)
            searchBar.animate()
                    .translationY(-overlap)
                    .alpha(1f)
                    .setDuration(140)
                    .start();
        } else {
            // volta ao lugar original
            searchBar.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(140)
                    .start();
        }
    }

    private int dp(int v){
        return Math.round(getResources().getDisplayMetrics().density * v);
    }







}
