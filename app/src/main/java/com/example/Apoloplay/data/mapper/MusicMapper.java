// com/example/Apoloplay/data/mapper/MusicMapper.java
package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.SpotifyTrackDTO;
import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.domain.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicMapper {

    // --- JÁ EXISTENTE: Search (mantido) ---
    public static List<Music> toDomain(SpotifySearchResponseDTO dto){
        List<Music> out = new ArrayList<>();
        if (dto == null || dto.tracks == null || dto.tracks.items == null) return out;

        for (SpotifySearchResponseDTO.Item it : dto.tracks.items) {
            String artist = (it.artists!=null && !it.artists.isEmpty()) ? it.artists.get(0).name : "";
            String cover  = (it.album!=null && it.album.images!=null && !it.album.images.isEmpty()) ? it.album.images.get(0).url : "";
            String album  = (it.album!=null ? it.album.name : "");
            String rel    = (it.album!=null ? it.album.releaseDate : "");

            out.add(new Music(
                    nz(it.name),
                    nz(artist),
                    nz(cover),
                    nz(it.previewUrl),
                    nz(album),
                    nz(rel),
                    nz(it.uri) // spotify:track:...
            ));
        }
        return out;
    }

    // --- NOVO: Playlist tracks -> Domain (usado pelo TrendingRepository) ---
    public List<Music> fromPlaylistTracks(PlaylistTracksResponseDTO dto) {
        List<Music> out = new ArrayList<>();
        if (dto == null || dto.items == null) return out;

        for (PlaylistTrackItemDTO item : dto.items) {
            if (item == null || item.track == null) continue;
            SpotifyTrackDTO t = item.track;

            String artist = (t.artists != null && !t.artists.isEmpty() && t.artists.get(0) != null)
                    ? nz(t.artists.get(0).name) : "";

            String cover = (t.album != null && t.album.images != null && !t.album.images.isEmpty() && t.album.images.get(0) != null)
                    ? nz(t.album.images.get(0).url) : "";

            String album = (t.album != null) ? nz(t.album.name) : "";
            String rel   = (t.album != null) ? nz(t.album.releaseDate) : "";

            out.add(new Music(
                    nz(t.name),
                    nz(artist),
                    nz(cover),
                    nz(t.previewUrl),
                    nz(album),
                    nz(rel),
                    nz(t.uri) // spotify:track:...
            ));
        }
        return out;
    }

    private static String nz(String s){ return s == null ? "" : s; }
}
