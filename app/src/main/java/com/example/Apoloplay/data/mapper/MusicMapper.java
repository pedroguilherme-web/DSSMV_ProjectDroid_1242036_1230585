package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.SpotifyTrackDTO;
import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.data.model.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para transformar respostas da Spotify Web API em Music (domínio).
 * ORDEM DO CONSTRUTOR DE Music:
 *   (title, artist, imageUrl, previewUrl, albumName, releaseDate, spotifyTrackUri)
 */
public class MusicMapper {

    /** Search -> lista de Music */
    public static List<Music> toDomain(SpotifySearchResponseDTO dto) {
        List<Music> out = new ArrayList<>();
        if (dto == null || dto.tracks == null || dto.tracks.items == null) return out;

        for (SpotifySearchResponseDTO.Item it : dto.tracks.items) {
            if (it == null) continue;

            String title   = nz(it.name);
            String artist  = (it.artists != null && !it.artists.isEmpty() && it.artists.get(0) != null)
                    ? nz(it.artists.get(0).name) : "";
            String image   = (it.album != null && it.album.images != null && !it.album.images.isEmpty() && it.album.images.get(0) != null)
                    ? nz(it.album.images.get(0).url) : "";
            String album   = (it.album != null) ? nz(it.album.name) : "";
            String rel     = (it.album != null) ? nz(it.album.releaseDate) : "";
            String preview = nz(it.previewUrl);
            String uri     = nz(it.uri);

            // Ordem EXATA do teu construtor
            out.add(new Music(title, artist, image, preview, album, rel, uri));
        }
        return out;
    }

    /** Playlist tracks (ex.: Trending/PlaylistDetails) -> lista de Music */
    public List<Music> fromPlaylistTracks(PlaylistTracksResponseDTO dto) {
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
