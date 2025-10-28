package com.example.Apoloplay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Apoloplay.models.Music;
import com.squareup.picasso.Picasso; // Assumindo que você usa Picasso ou Glide para carregar imagens

import java.util.List;

/**
 * Adaptador para exibir a lista de objetos Music no RecyclerView.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<Music> musicList;

    public MusicAdapter(List<Music> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⚠️ Necessita de um layout item_music.xml (que você deve criar)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);

        holder.titleTextView.setText(music.getTitle());
        holder.artistTextView.setText(music.getArtist());

        // Carregar imagem usando Picasso (assumindo que você o adicionou ao Gradle)
        if (music.getImageUrl() != null && !music.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(music.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    // ViewHolder
    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView artistTextView;
        ImageView coverImageView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            // ⚠️ IDs de views (que devem estar em item_music.xml)
            titleTextView = itemView.findViewById(R.id.music_title);
            artistTextView = itemView.findViewById(R.id.music_artist);
            coverImageView = itemView.findViewById(R.id.music_cover);
        }
    }
}