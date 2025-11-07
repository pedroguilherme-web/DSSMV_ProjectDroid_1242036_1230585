package com.example.Apoloplay.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.ui.playlistdetails.PlaylistDetailsUiState;
import com.example.Apoloplay.ui.playlistdetails.PlaylistDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_PLAYLIST_ID = "EXTRA_FROM_PLAYLIST_ID";

    private PlaylistDetailsViewModel vm;
    private SwipeRefreshLayout swipe;
    private TracksAdapter adapter;
    private String playlistId;
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ usa o layout certo
        setContentView(R.layout.activity_playlist_details);

        playlistId = getIntent().getStringExtra(PlaylistsActivity.EXTRA_PLAYLIST_ID);
        playlistName = getIntent().getStringExtra(PlaylistsActivity.EXTRA_PLAYLIST_NAME);
        setTitle(playlistName != null ? playlistName : "Playlist");

        vm = new ViewModelProvider(this).get(PlaylistDetailsViewModel.class);

        swipe = findViewById(R.id.swipe);
        RecyclerView rv = findViewById(R.id.rv_tracks);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Ao clicar numa música → abre DetailsActivity, passando também o playlistId
        adapter = new TracksAdapter(m -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra("MUSIC_DETAILS", m);
            i.putExtra(EXTRA_FROM_PLAYLIST_ID, playlistId); // ⚙️ poderá remover desta playlist
            startActivity(i);
        });
        rv.setAdapter(adapter);

        vm.getState().observe(this, this::render);

        swipe.setOnRefreshListener(() -> vm.load(playlistId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        vm.load(playlistId);
    }

    private void render(PlaylistDetailsUiState s) {
        swipe.setRefreshing(s.loading);
        adapter.submit(s.tracks);
        if (s.error != null) Toast.makeText(this, s.error, Toast.LENGTH_SHORT).show();
    }

    // Adapter simples (sem botão remover; o remover é pelo ⚙️ do DetailsActivity)
    static class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.VH> {
        interface Click { void onClick(Music m); }
        private final Click click;
        private List<Music> data = new ArrayList<>();

        TracksAdapter(Click c) { click = c; }

        void submit(List<Music> d) {
            data = (d != null) ? d : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup p, int v) {
            android.view.View view = android.view.LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_music, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Music m = data.get(pos);
            // ✅ IDs batem com o teu item_music.xml
            h.title.setText(m.getTitle());
            h.artist.setText(m.getArtist());

            ImageView cover = h.itemView.findViewById(R.id.music_cover);
            if (cover != null && m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
                com.squareup.picasso.Picasso.get().load(m.getImageUrl()).into(cover);
            }



            h.itemView.setOnClickListener(v -> click.onClick(m));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final android.widget.TextView title, artist;
            VH(android.view.View itemView) {
                super(itemView);
                // ✅ usa music_title / music_artist (os teus IDs)
                title  = itemView.findViewById(R.id.music_title);
                artist = itemView.findViewById(R.id.music_artist);
            }
        }
    }
}
