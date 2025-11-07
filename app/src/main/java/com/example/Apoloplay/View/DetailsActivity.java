// com/example/Apoloplay/View/DetailsActivity.java
package com.example.Apoloplay.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.player.PlayerUiState;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.playlistdetails.PlaylistDetailsViewModel;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    private PlayerViewModel playerVm;
    private PlaylistDetailsViewModel playlistVm; // <-- PRECISAS DISTO
    private Music music;

    private Button playPauseButton;
    private TextView connectionStatusText;
    private ImageButton settingsBtn;

    public static final String EXTRA_FROM_PLAYLIST_ID = "EXTRA_FROM_PLAYLIST_ID";
    private String openedFromPlaylistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        playPauseButton = findViewById(R.id.btn_play_pause);
        connectionStatusText = findViewById(R.id.connection_status);
        settingsBtn = findViewById(R.id.btn_settings);

        ImageView cover = findViewById(R.id.detail_music_cover);
        TextView title = findViewById(R.id.detail_music_title);
        TextView artist = findViewById(R.id.detail_music_artist);
        TextView album = findViewById(R.id.detail_music_album);
        TextView rel   = findViewById(R.id.detail_music_release_date);

        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        playlistVm = new ViewModelProvider(this).get(PlaylistDetailsViewModel.class); // <-- AQUI

        music = (Music) getIntent().getSerializableExtra("MUSIC_DETAILS");
        openedFromPlaylistId = getIntent().getStringExtra(EXTRA_FROM_PLAYLIST_ID);

        if (music != null) {
            if (music.getImageUrl() != null && !music.getImageUrl().isEmpty()) {
                Picasso.get().load(music.getImageUrl()).into(cover);
            }
            title.setText(music.getTitle());
            artist.setText(music.getArtist());
            album.setText("Álbum: " + (music.getAlbumName() != null ? music.getAlbumName() : "Desconhecido"));
            rel.setText("Lançamento: " + (music.getReleaseDate() != null ? music.getReleaseDate() : "N/D"));
        }

        playerVm.getState().observe(this, this::renderState);
        playPauseButton.setOnClickListener(v -> { if (music != null) playerVm.playOrToggle(this, music); });
        settingsBtn.setOnClickListener(this::showSettingsMenu);
    }

    private void renderState(PlayerUiState s) {
        if (s.error != null) {
            connectionStatusText.setText("⚠️ " + s.error);
        } else if (s.connected) {
            connectionStatusText.setText("✅ Conectado ao Spotify");
        } else {
            connectionStatusText.setText("🔄 A ligar ao Spotify…");
        }
        playPauseButton.setEnabled(true);
        playPauseButton.setText(s.isPlaying ? "⏸️ Pausar" : "▶️ Tocar");
    }

    private void showSettingsMenu(View anchor) {
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_detail_settings, popup.getMenu());

        PlayerUiState cur = playerVm.getState().getValue();
        boolean loop = cur != null && cur.loopOne;
        popup.getMenu().findItem(R.id.action_repeat_one).setChecked(loop);

        if (popup.getMenu().findItem(R.id.action_remove_from_playlist) != null) {
            popup.getMenu().findItem(R.id.action_remove_from_playlist)
                    .setVisible(openedFromPlaylistId != null);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_repeat_one) {
                boolean enable = !item.isChecked();
                item.setChecked(enable);
                playerVm.setRepeatOne(enable);
                return true;

            } else if (id == R.id.action_add_to_playlist) {
                if (music != null && music.getSpotifyTrackUri() != null) {
                    com.example.Apoloplay.ui.addtoplaylist.AddToPlaylistSheet
                            .newInstance(music.getSpotifyTrackUri())
                            .show(getSupportFragmentManager(), "AddToPlaylist");
                } else {
                    Toast.makeText(this, "Sem track URI.", Toast.LENGTH_SHORT).show();
                }
                return true;

            } else if (id == R.id.action_remove_from_playlist) {
                if (openedFromPlaylistId != null && music != null && music.getSpotifyTrackUri() != null) {
                    playlistVm.remove(openedFromPlaylistId, music.getSpotifyTrackUri(), () -> {
                        Toast.makeText(this, "Música removida", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override protected void onStart() {
        super.onStart();
        playerVm.connect(this);
    }

    @Override protected void onStop() {
        super.onStop();
        playerVm.disconnect();
    }
}
