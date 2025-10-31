package com.example.Apoloplay.Model;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Apoloplay.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adaptador para exibir a lista de objetos Music no RecyclerView.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Music> musicList;

    public MusicAdapter(List<Music> musicList) {
        this.musicList = musicList;
    }


    public void setMusicList(List<Music> newMusicList) {
        this.musicList = newMusicList;

    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);

        holder.titleTextView.setText(music.getTitle());
        holder.artistTextView.setText(music.getArtist());


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
            titleTextView = itemView.findViewById(R.id.music_title);
            artistTextView = itemView.findViewById(R.id.music_artist);
            coverImageView = itemView.findViewById(R.id.music_cover);
        }
    }


}