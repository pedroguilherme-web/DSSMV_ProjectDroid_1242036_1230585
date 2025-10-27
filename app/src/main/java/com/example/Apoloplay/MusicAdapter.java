package com.example.Apoloplay.ui;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.Apoloplay.R
import com.example.Apoloplay.models.Music
import com.squareup.picasso.Picasso

class MusicAdapter(private val musicList: List<Music>) :
        RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cover: ImageView = view.findViewById(R.id.coverImageView)
    val title: TextView = view.findViewById(R.id.titleTextView)
    val artist: TextView = view.findViewById(R.id.artistTextView)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
    return MusicViewHolder(view)
}

override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
    val music = musicList[position]
    holder.title.text = music.title
    holder.artist.text = music.artist
    Picasso.get().load(music.coverUrl).into(holder.cover)
}

override fun getItemCount() = musicList.size
}