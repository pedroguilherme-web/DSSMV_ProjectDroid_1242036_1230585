package com.example.Apoloplay.ui.addtoplaylist;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Playlist;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylistSheet extends BottomSheetDialogFragment {

    private static final String ARG_TRACK_URI = "arg_track_uri";

    /** Factory (um argumento) — o teu DetailsActivity chama este. */
    public static AddToPlaylistSheet newInstance(String trackUri) {
        Bundle b = new Bundle();
        b.putString(ARG_TRACK_URI, trackUri);
        AddToPlaylistSheet f = new AddToPlaylistSheet();
        f.setArguments(b);
        return f;
    }

    private AddToPlaylistViewModel vm;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView empty;
    private MaterialButton btnCreate;
    private PlaylistsAdapter adapter;
    private String trackUri;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_to_playlist, container, false);
        rv = v.findViewById(R.id.rv_playlists);
        progress = v.findViewById(R.id.progress);
        empty = v.findViewById(R.id.empty);
        btnCreate = v.findViewById(R.id.btn_create_playlist);

        adapter = new PlaylistsAdapter(new ArrayList<>(), playlist -> {
            if (playlist != null && trackUri != null) {
                // ✅ modelos encapsulados
                vm.addTrack(playlist.getId(), trackUri);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        btnCreate.setOnClickListener(v1 -> showCreateDialog());
        return v;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trackUri = getArguments() != null ? getArguments().getString(ARG_TRACK_URI) : null;

        vm = new ViewModelProvider(this).get(AddToPlaylistViewModel.class);

        vm.getLoading().observe(getViewLifecycleOwner(),
                loading -> progress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        vm.getPlaylists().observe(getViewLifecycleOwner(), list -> {
            boolean isEmpty = (list == null || list.isEmpty());
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            adapter.submit(list);
        });

        vm.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty())
                Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
        });

        vm.getAddSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(getContext(), "Adicionada à playlist!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        vm.getCreatedPlaylist().observe(getViewLifecycleOwner(), created -> {
            if (created != null) {
                Toast.makeText(getContext(), "Playlist criada", Toast.LENGTH_SHORT).show();
                vm.refresh(); // refaz a lista para aparecer a nova
            }
        });

        // arranca a lista
        vm.refresh();
    }

    private void showCreateDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Nome da playlist");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Criar nova playlist")
                .setView(input)
                .setPositiveButton("Criar", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) vm.create(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Adapter simples
    static class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.VH> {
        interface OnClick { void click(Playlist p); }
        private List<Playlist> data;
        private final OnClick onClick;
        PlaylistsAdapter(List<Playlist> d, OnClick c){ data=d; onClick=c; }

        void submit(List<Playlist> d){ data = d!=null ? d : new ArrayList<>(); notifyDataSetChanged(); }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v){
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_playlist_row, p, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos){
            Playlist pl = data.get(pos);
            // ✅ getters (antes: pl.name)
            h.title.setText(pl.getName());
            h.itemView.setOnClickListener(v -> onClick.click(pl));
        }
        @Override public int getItemCount(){ return data!=null ? data.size() : 0; }

        static class VH extends RecyclerView.ViewHolder{
            TextView title;
            VH(@NonNull View itemView){ super(itemView); title = itemView.findViewById(R.id.tv_playlist_name); }
        }
    }
}
