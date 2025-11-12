package com.example.Apoloplay.View;

import android.content.Context;
import android.content.Intent;
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
import com.example.Apoloplay.ui.addtoplaylist.AddToPlaylistSheet;
import com.example.Apoloplay.ui.details.DetailsUiState;
import com.example.Apoloplay.ui.details.DetailsViewModel;
import com.example.Apoloplay.ui.player.PlayerUiState;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.playlistdetails.PlaylistDetailsViewModel;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_PLAYLIST_ID = "EXTRA_FROM_PLAYLIST_ID";
    public static final String EXTRA_MUSIC = "MUSIC_DETAILS";

    private PlayerViewModel playerVm;
    private PlaylistDetailsViewModel playlistVm;
    private DetailsViewModel detailsVm;

    private ImageView cover;
    private TextView title, artist, album, rel, connectionStatusText;
    private Button playPauseButton;
    private ImageButton settingsBtn;

    private String openedFromPlaylistId;

    public static void start(Context ctx, Music m, String source){
        Intent i = new Intent(ctx, DetailsActivity.class);
        i.putExtra(EXTRA_MUSIC, m);
        ctx.startActivity(i);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        cover = findViewById(R.id.detail_music_cover);
        title = findViewById(R.id.detail_music_title);
        artist = findViewById(R.id.detail_music_artist);
        album = findViewById(R.id.detail_music_album);
        rel   = findViewById(R.id.detail_music_release_date);
        playPauseButton = findViewById(R.id.btn_play_pause);
        connectionStatusText = findViewById(R.id.connection_status);
        settingsBtn = findViewById(R.id.btn_settings);

        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        playlistVm = new ViewModelProvider(this).get(PlaylistDetailsViewModel.class);
        detailsVm  = new ViewModelProvider(this).get(DetailsViewModel.class);

        Music initial = (Music) getIntent().getSerializableExtra(EXTRA_MUSIC);
        openedFromPlaylistId = getIntent().getStringExtra(EXTRA_FROM_PLAYLIST_ID);

        detailsVm.init(initial);
        detailsVm.enrichIfNeeded(
                initial!=null ? initial.getTitle() : null,
                initial!=null ? initial.getArtist(): null
        );
        if (openedFromPlaylistId != null) {
            // só vai marcar true quando já houver URI
            detailsVm.checkInPlaylist(openedFromPlaylistId);
        }

        playerVm.getState().observe(this, this::renderPlayer);
        detailsVm.getState().observe(this, this::renderDetails);

        playPauseButton.setOnClickListener(v -> {
            DetailsUiState s = detailsVm.getState().getValue();
            if (s==null || s.music==null){
                Toast.makeText(this, "Sem faixa.", Toast.LENGTH_SHORT).show(); return;
            }
            playerVm.toggle(this, s.music);
        });

        settingsBtn.setOnClickListener(this::showSettingsMenu);
    }

    private void renderPlayer(PlayerUiState s){
        if (s.error != null) connectionStatusText.setText("⚠️ " + s.error);
        else if (s.connected) connectionStatusText.setText("✅ Conectado ao Spotify");
        else connectionStatusText.setText("🔄 A ligar ao Spotify…");
        playPauseButton.setEnabled(true);
        playPauseButton.setText(s.isPlaying ? "⏸️ Pausar" : "▶️ Tocar");
    }

    private void renderDetails(DetailsUiState s){
        if (s==null || s.music==null) return;
        if (s.music.getImageUrl()!=null && !s.music.getImageUrl().isEmpty())
            Picasso.get().load(s.music.getImageUrl()).into(cover);
        else cover.setImageDrawable(null);

        title.setText(s.music.getTitle()!=null ? s.music.getTitle() : "");
        artist.setText(s.music.getArtist()!=null ? s.music.getArtist() : "");
        album.setText("Álbum: " + (s.music.getAlbumName()!=null ? s.music.getAlbumName() : "Desconhecido"));
        rel.setText("Lançamento: " + (s.music.getReleaseDate()!=null ? s.music.getReleaseDate() : "N/D"));
    }

    private void showSettingsMenu(View anchor){
        DetailsUiState st = detailsVm.getState().getValue();
        boolean hasUri = st!=null && st.music!=null && st.music.getSpotifyTrackUri()!=null && !st.music.getSpotifyTrackUri().isEmpty();

        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_detail_settings, popup.getMenu());

        if (popup.getMenu().findItem(R.id.action_add_to_playlist) != null)
            popup.getMenu().findItem(R.id.action_add_to_playlist).setVisible(hasUri);

        if (popup.getMenu().findItem(R.id.action_remove_from_playlist) != null)
            popup.getMenu().findItem(R.id.action_remove_from_playlist)
                    .setVisible(openedFromPlaylistId != null && hasUri && st!=null && st.inPlaylist);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_add_to_playlist) {
                AddToPlaylistSheet.newInstance(st.music.getSpotifyTrackUri())
                        .show(getSupportFragmentManager(), "AddToPlaylist");
                return true;
            } else if (id == R.id.action_remove_from_playlist) {
                playlistVm.remove(openedFromPlaylistId, st.music.getSpotifyTrackUri(), () -> {
                    Toast.makeText(this, "Música removida", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override protected void onStart() { super.onStart(); playerVm.connect(this); }
    @Override protected void onStop()  { super.onStop();  playerVm.disconnect(); }
}
