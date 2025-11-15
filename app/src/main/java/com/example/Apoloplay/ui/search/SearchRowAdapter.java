package com.example.Apoloplay.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.example.Apoloplay.data.model.Music;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchRowAdapter extends RecyclerView.Adapter<SearchRowAdapter.VH> {

    public interface Click { void onClick(Music m); }

    private final Click click;
    private List<Music> data = new ArrayList<>();

    public SearchRowAdapter(Click c){ this.click = c; }

    public void submit(List<Music> d){
        data = (d!=null)? d : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        android.view.View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_music, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Music m = data.get(pos);
        h.title.setText(m.getTitle());
        h.artist.setText(m.getArtist());
        if (m.getImageUrl()!=null && !m.getImageUrl().isEmpty())
            Picasso.get().load(m.getImageUrl()).into(h.cover);
        h.itemView.setOnClickListener(v -> click.onClick(m));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title, artist;
        VH(@NonNull android.view.View itemView){
            super(itemView);
            cover  = itemView.findViewById(R.id.music_cover);
            title  = itemView.findViewById(R.id.music_title);
            artist = itemView.findViewById(R.id.music_artist);
        }
    }
}
