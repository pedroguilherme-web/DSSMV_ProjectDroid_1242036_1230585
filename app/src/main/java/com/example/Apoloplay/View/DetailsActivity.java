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
import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.SpotifyTrackDTO;
import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.addtoplaylist.AddToPlaylistSheet;
import com.example.Apoloplay.ui.player.PlayerUiState;
import com.example.Apoloplay.ui.player.PlayerViewModel;
import com.example.Apoloplay.ui.playlistdetails.PlaylistDetailsViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_PLAYLIST_ID = "EXTRA_FROM_PLAYLIST_ID";
    public static final String EXTRA_MUSIC = "MUSIC_DETAILS";
    public static final String EXTRA_SOURCE = "SOURCE"; // opcional

    private PlayerViewModel playerVm;
    private PlaylistDetailsViewModel playlistVm;

    private ImageView cover;
    private TextView title, artist, album, rel, connectionStatusText;
    private Button playPauseButton;
    private ImageButton settingsBtn;

    private Music music;                    // domain model (pode ser enriquecido)
    private String openedFromPlaylistId;    // se vieste de uma playlist
    private boolean inThisPlaylist = false; // resultado do check

    // Retrofit / Execução em IO
    private final SpotifyService spotifyApi = ServiceLocator.spotifyService();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // ---------- Navegação helper (mantém o teu método) ----------
    public static void start(Context ctx, Music m, String source) {
        Intent i = new Intent(ctx, DetailsActivity.class);
        i.putExtra(EXTRA_MUSIC, m);
        i.putExtra(EXTRA_SOURCE, source);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // binds
        playPauseButton     = findViewById(R.id.btn_play_pause);
        connectionStatusText= findViewById(R.id.connection_status);
        settingsBtn         = findViewById(R.id.btn_settings);
        cover               = findViewById(R.id.detail_music_cover);
        title               = findViewById(R.id.detail_music_title);
        artist              = findViewById(R.id.detail_music_artist);
        album               = findViewById(R.id.detail_music_album);
        rel                 = findViewById(R.id.detail_music_release_date);

        playerVm   = new ViewModelProvider(this).get(PlayerViewModel.class);
        playlistVm = new ViewModelProvider(this).get(PlaylistDetailsViewModel.class);

        music = (Music) getIntent().getSerializableExtra(EXTRA_MUSIC);
        openedFromPlaylistId = getIntent().getStringExtra(EXTRA_FROM_PLAYLIST_ID);

        // Render inicial com o que já tens
        if (music != null) renderMusic(music);

        // Estado do player
        playerVm.getState().observe(this, this::renderState);

        // Play/Pause
        playPauseButton.setOnClickListener(v -> {
            if (music == null) {
                Toast.makeText(this, "Sem faixa selecionada.", Toast.LENGTH_SHORT).show();
                return;
            }
            playerVm.toggle(this, music);
        });

        // Menu ⚙️
        settingsBtn.setOnClickListener(this::showSettingsMenu);

        // Se ainda não há URI Spotify (caso Shazam tenha trazido só title/artist), enriquecemos
        if (music != null && (music.getSpotifyTrackUri() == null || music.getSpotifyTrackUri().isEmpty())) {
            enrichFromSpotifyFirstMatch(music.getTitle(), music.getArtist());
        } else {
            // Já há URI: se vieste de playlist, verifica se já está lá
            checkInThisPlaylistIfNeeded();
        }
    }

    @Override protected void onStart() { super.onStart(); playerVm.connect(this); }
    @Override protected void onStop()  { super.onStop();  playerVm.disconnect(); }

    // ---------- UI ----------
    private void renderMusic(Music m) {
        if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            Picasso.get().load(m.getImageUrl()).into(cover);
        } else {
            cover.setImageDrawable(null);
        }
        title.setText(m.getTitle() != null ? m.getTitle() : "");
        artist.setText(m.getArtist() != null ? m.getArtist() : "");
        album.setText("Álbum: " + (m.getAlbumName() != null ? m.getAlbumName() : "Desconhecido"));
        rel.setText("Lançamento: " + (m.getReleaseDate() != null ? m.getReleaseDate() : "N/D"));
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

        boolean hasUri = (music != null && music.getSpotifyTrackUri() != null && !music.getSpotifyTrackUri().isEmpty());

        if (popup.getMenu().findItem(R.id.action_add_to_playlist) != null)
            popup.getMenu().findItem(R.id.action_add_to_playlist).setVisible(hasUri);

        if (popup.getMenu().findItem(R.id.action_remove_from_playlist) != null)
            popup.getMenu().findItem(R.id.action_remove_from_playlist)
                    .setVisible(openedFromPlaylistId != null && hasUri && inThisPlaylist);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_add_to_playlist) {
                if (!hasUri) {
                    Toast.makeText(this, "Sem track URI.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                AddToPlaylistSheet.newInstance(music.getSpotifyTrackUri())
                        .show(getSupportFragmentManager(), "AddToPlaylist");
                return true;

            } else if (id == R.id.action_remove_from_playlist) {
                if (openedFromPlaylistId != null && hasUri) {
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

    // ---------- Enriquecer com 1.º resultado do Spotify ----------
    private void enrichFromSpotifyFirstMatch(String mTitle, String mArtist) {
        if (mTitle == null || mArtist == null) return;

        final String q = buildQuery(mTitle, mArtist); // ex.: track:"Believer" artist:"Imagine Dragons"
        final String bearer = "Bearer " + ServiceLocator.sessionProvider().getUserAccessToken();

        io.execute(() -> {
            try {
                Response<SpotifySearchResponseDTO> resp =
                        spotifyApi.searchTracks(bearer, q, "track", 1).execute();

                if (!resp.isSuccessful() || resp.body() == null ||
                        resp.body().tracks == null ||
                        resp.body().tracks.items == null ||
                        resp.body().tracks.items.isEmpty()) {
                    return;
                }

                // O tipo certo é SpotifySearchResponseDTO.Item
                SpotifySearchResponseDTO.Item it = resp.body().tracks.items.get(0);

                String newTitle = it.name;
                String newArtist = (it.artists != null && !it.artists.isEmpty() && it.artists.get(0) != null)
                        ? it.artists.get(0).name : mArtist;
                String imageUrl = (it.album != null && it.album.images != null && !it.album.images.isEmpty() && it.album.images.get(0) != null)
                        ? it.album.images.get(0).url : null;
                String albumName = (it.album != null) ? it.album.name : null;
                String releaseDate = (it.album != null) ? it.album.releaseDate : null; // campo em camelCase
                String uri = it.uri; // já vem no item de search
                String preview = it.previewUrl;

                // Atualiza o domain Music
                music = new Music(
                        newTitle != null ? newTitle : mTitle,
                        newArtist,
                        imageUrl,
                        preview,
                        albumName,
                        releaseDate,
                        uri
                );

                runOnUiThread(() -> {
                    renderMusic(music);
                    // Agora que temos URI, se vieste de playlist, verifica se está lá para mostrar "Remover"
                    checkInThisPlaylistIfNeeded();
                });

            } catch (Exception ignore) { /* silencia erro de rede leve */ }
        });
    }

    private String buildQuery(String title, String artist) {
        String t = title != null ? title.replaceAll("\\(.*?\\)", "").trim() : "";
        String a = artist != null ? artist.replaceAll("\\(.*?\\)", "").trim() : "";
        return "track:\"" + t + "\" artist:\"" + a + "\"";
    }

    // ---------- Verificar se já existe na playlist (pagina até encontrar) ----------
    private void checkInThisPlaylistIfNeeded() {
        if (openedFromPlaylistId == null) return;
        if (music == null || music.getSpotifyTrackUri() == null || music.getSpotifyTrackUri().isEmpty()) return;

        final String playlistId = openedFromPlaylistId;
        final String targetUri  = music.getSpotifyTrackUri();
        final String bearer     = "Bearer " + ServiceLocator.sessionProvider().getUserAccessToken();

        io.execute(() -> {
            boolean found = false;
            int limit = 100, offset = 0;

            try {
                while (!found) {
                    Response<PlaylistTracksResponseDTO> r =
                            spotifyApi.getPlaylistTracks(bearer, playlistId, limit, offset).execute();

                    if (!r.isSuccessful() || r.body() == null || r.body().items == null) break;

                    List<PlaylistTrackItemDTO> items = r.body().items;
                    if (items.isEmpty()) break;

                    for (PlaylistTrackItemDTO it : items) {
                        SpotifyTrackDTO t = (it != null) ? it.track : null; // campo "track" no item
                        if (t != null && targetUri.equals(t.uri)) {
                            found = true; break;
                        }
                    }

                    if (found || items.size() < limit) break;
                    offset += limit;
                }
            } catch (Exception ignore) { }

            final boolean result = found;
            runOnUiThread(() -> inThisPlaylist = result);
        });
    }
}
