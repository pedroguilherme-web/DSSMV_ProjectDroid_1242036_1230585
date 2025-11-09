package com.example.Apoloplay.ui.playlists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.domain.model.Playlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter simples para o layout item_playlist_row.xml
 * - Mostra nome e número de músicas
 * - Clique curto → abre detalhes
 * - Clique longo → abre menu contextual
 */
public class PlaylistRowAdapter extends RecyclerView.Adapter<PlaylistRowAdapter.VH> {

    public interface OnClick { void onClick(Playlist p); }
    public interface OnLongClick { void onLongClick(View anchor, Playlist p); }

    private final OnClick click;
    private final OnLongClick longClick;
    private List<Playlist> data = new ArrayList<>();

    public PlaylistRowAdapter(OnClick click, OnLongClick longClick) {
        this.click = click;
        this.longClick = longClick;
    }

    public void submit(List<Playlist> d) {
        data = (d != null) ? d : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Playlist pl = data.get(position);
        h.title.setText(pl.getName() + " (" + pl.getTracksTotal() + ")");

        h.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(pl);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (longClick != null) longClick.onLongClick(v, pl);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_playlist_name);
        }
    }
}
