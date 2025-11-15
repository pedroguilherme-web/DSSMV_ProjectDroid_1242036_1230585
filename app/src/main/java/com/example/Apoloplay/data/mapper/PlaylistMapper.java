package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.dto.PlaylistDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistsResponseDTO;
import com.example.Apoloplay.data.remote.dto.SpotifyTrackDTO;
import com.example.Apoloplay.data.model.Music;
import com.example.Apoloplay.data.model.Playlist;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para Playlists e suas tracks.
 * Mantém a ordem do construtor de Music:
 *   (title, artist, imageUrl, previewUrl, albumName, releaseDate, spotifyTrackUri)
 */
public class PlaylistMapper {

    public static List<Playlist> toDomain(PlaylistsResponseDTO dto) {
        List<Playlist> out = new ArrayList<>();
        if (dto == null || dto.items == null) return out;

        for (PlaylistDTO p : dto.items) {
            if (p == null) continue;
            int total = (p.tracks != null) ? p.tracks.total : 0;
            out.add(new Playlist(p.id, p.name, total));
        }
        return out;
    }

    public static Playlist toDomain(PlaylistDTO dto) {
        int total = (dto != null && dto.tracks != null) ? dto.tracks.total : 0;
        return new Playlist(dto.id, dto.name, total);
    }

    /** Tracks de uma playlist -> lista de Music (usado em PlaylistDetails, Trending, etc.) */
    public static List<Music> tracksToDomain(PlaylistTracksResponseDTO dto) {
        List<Music> out = new ArrayList<>();
        if (dto == null || dto.items == null) return out;

        for (PlaylistTrackItemDTO item : dto.items) {
            if (item == null || item.track == null) continue;
            SpotifyTrackDTO t = item.track;

            String title   = nz(t.name);
            String artist  = (t.artists != null && !t.artists.isEmpty() && t.artists.get(0) != null)
                    ? nz(t.artists.get(0).name) : "";
            String image   = (t.album != null && t.album.images != null && !t.album.images.isEmpty() && t.album.images.get(0) != null)
                    ? nz(t.album.images.get(0).url) : "";
            String album   = (t.album != null) ? nz(t.album.name) : "";
            String rel     = (t.album != null) ? nz(t.album.releaseDate) : "";
            String preview = nz(t.previewUrl);
            String uri     = nz(t.uri);

            out.add(new Music(title, artist, image, preview, album, rel, uri));
        }

        return out;
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
