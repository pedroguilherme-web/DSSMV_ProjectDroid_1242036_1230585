// com/example/Apoloplay/ui/trending/TrendingCarouselAdapter.java
package com.example.Apoloplay.ui.trending;

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

/** Adapter do carrossel (loop visual com multiplicador). */
public class TrendingCarouselAdapter extends RecyclerView.Adapter<TrendingCarouselAdapter.VH> {

    public interface OnClick { void onClick(Music m); }

    private final OnClick onClick;
    private List<Music> items = new ArrayList<>();

    private static final int MULTIPLIER = 1000; // lista virtual grande

    public TrendingCarouselAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<Music> data) {
        items = (data != null) ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Tamanho lógico (lista real). */
    public int logicalSize() { return items == null ? 0 : items.size(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_music, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        int n = logicalSize();
        if (n == 0) return;
        int actual = position % n;
        Music m = items.get(actual);

        h.title.setText(m.getTitle());
        if (m.getImageUrl() != null && !m.getImageUrl().isEmpty()) {
            Picasso.get().load(m.getImageUrl()).into(h.cover);
        } else {
            h.cover.setImageResource(R.drawable.ic_placeholder);
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(m));
    }

    @Override public int getItemCount() {
        int n = logicalSize();
        return n == 0 ? 0 : n * MULTIPLIER;
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title;
        VH(@NonNull android.view.View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.img_cover);
            title = itemView.findViewById(R.id.txt_title);
        }
    }
}
