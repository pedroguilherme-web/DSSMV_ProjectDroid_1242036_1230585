// com/example/Apoloplay/View/MainActivity.java
package com.example.Apoloplay.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.ui.player.PlayerViewModel; // <— usa o VM novo

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1337;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    private PlayerViewModel playerVm; // <— VM novo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        playerVm = new ViewModelProvider(this).get(PlayerViewModel.class);

        ImageButton btnOpenPlaylists = findViewById(R.id.btn_open_playlists);
        //ImageButton btnOpenSearch    = findViewById(R.id.btn_open_search);

        btnOpenPlaylists.setOnClickListener(v -> ensureLoginThen(() -> {
            playerVm.connect(this);     // App Remote via VM novo
            goToPlaylists();
        }));
/*
        if (btnOpenSearch != null) {
            btnOpenSearch.setOnClickListener(v -> ensureLoginThen(() -> {
                // Search usa token apenas para playlists (quando adicionas faixas),
                // mas garantimos que o token já está no SessionProvider.
                goToSearch();

            }));


        }
         */
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

        AuthorizationClient.openLoginActivity(this, REQ_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQ_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                String accessToken = response.getAccessToken();

                // Guardar no provider partilhado usado pela arquitetura nova
                ServiceLocator.sessionProvider().setUserAccessToken(accessToken);

                // Opcional: conecta já o App Remote para evitar esperar no primeiro click
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

    private void goToSearch() {
        startActivity(new Intent(this, SearchActivity.class));
    }
}
