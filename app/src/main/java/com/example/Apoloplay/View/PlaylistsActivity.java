
package com.example.Apoloplay.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Playlist;
import com.example.Apoloplay.ui.playlists.PlaylistRowAdapter;
import com.example.Apoloplay.ui.playlists.PlaylistsUiState;
import com.example.Apoloplay.ui.playlists.PlaylistsViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PlaylistsActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "EXTRA_PLAYLIST_ID";
    public static final String EXTRA_PLAYLIST_NAME = "EXTRA_PLAYLIST_NAME";

    private PlaylistsViewModel vm;
    private SwipeRefreshLayout swipe;
    private PlaylistRowAdapter adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        vm = new ViewModelProvider(this).get(PlaylistsViewModel.class);
        swipe = findViewById(R.id.swipe);

        RecyclerView rv = findViewById(R.id.rv_playlists);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlaylistRowAdapter(
                // tap → abre detalhes
                pl -> {
                    Intent i = new Intent(this, PlaylistDetailsActivity.class);
                    i.putExtra(EXTRA_PLAYLIST_ID, pl.id);
                    i.putExtra(EXTRA_PLAYLIST_NAME, pl.name);
                    startActivity(i);
                },
                // long-press → mostrar popup (este método está em baixo)
                this::showPlaylistMenu
        );
        rv.setAdapter(adapter);

        vm.getState().observe(this, this::render);
        swipe.setOnRefreshListener(vm::refresh);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    @Override protected void onStart() {
        super.onStart();
        vm.refresh(); // primeiro load
    }

    private void render(PlaylistsUiState s) {
        swipe.setRefreshing(s.loading);
        adapter.submit(s.items);
        if (s.error != null) Toast.makeText(this, s.error, Toast.LENGTH_SHORT).show();
    }

    // Recebe o anchor (View do item) + playlist, e mostra o menu
    private void showPlaylistMenu(android.view.View anchor, Playlist pl) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_playlist_row, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_playlist) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar playlist")
                        .setMessage("Queres eliminar \"" + pl.name + "\"?")
                        .setPositiveButton("Eliminar", (d, w) -> vm.delete(pl.id))
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}



