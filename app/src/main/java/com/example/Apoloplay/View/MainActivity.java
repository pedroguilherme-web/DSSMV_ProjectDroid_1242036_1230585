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
import com.example.Apoloplay.ViewModel.MusicViewModel;
import com.example.Apoloplay.data.ServiceLocator;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 1337;
    private static final String CLIENT_ID = "7b7105fe7abf4c13911b13a910f79cff";
    private static final String REDIRECT_URI = "com.example.apoloplay://callback";

    private MusicViewModel musicVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_auth);

        musicVM = new ViewModelProvider(this).get(MusicViewModel.class);

        ImageButton btnEye = findViewById(R.id.btn_open_playlists);
        btnEye.setOnClickListener(v -> {
            String token = ServiceLocator.sessionProvider().getUserAccessToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Inicia sessão no Spotify primeiro.", Toast.LENGTH_SHORT).show();
                startSpotifyLogin();
            } else {
                musicVM.connectSpotifyAppRemote(this);
                goToPlaylists();
            }
        });
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
            switch (response.getType()) {
                case TOKEN: {
                    String accessToken = response.getAccessToken();

                    // === NOVO: guardar no provider partilhado da arquitetura nova ===
                    ServiceLocator.sessionProvider().setUserAccessToken(accessToken);

                    // App Remote (não precisa do token do Web API)
                    musicVM.connectSpotifyAppRemote(this);

                    Toast.makeText(this, "Login Spotify bem-sucedido!", Toast.LENGTH_SHORT).show();
                    goToPlaylists();
                    break;
                }
                case ERROR: {
                    Toast.makeText(this, "Erro no login Spotify.", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    // cancelado → nada a fazer
            }
        }
    }

    private void goToPlaylists() {
        startActivity(new Intent(this, PlaylistsActivity.class));
    }
}
