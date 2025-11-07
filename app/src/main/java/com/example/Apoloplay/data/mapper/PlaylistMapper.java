package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.dto.*;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistMapper {

    public static List<Playlist> toDomain(PlaylistsResponseDTO dto){
        List<Playlist> out = new ArrayList<>();
        if (dto == null || dto.items == null) return out;
        for (PlaylistDTO p : dto.items) {
            int total = p != null && p.tracks != null ? p.tracks.total : 0;
            out.add(new Playlist(p.id, p.name, total));
        }
        return out;
    }

    public static Playlist toDomain(PlaylistDTO dto){
        int total = dto != null && dto.tracks != null ? dto.tracks.total : 0;
        return new Playlist(dto.id, dto.name, total);
    }










    public static List<Music> tracksToDomain(PlaylistTracksResponseDTO dto) {
        List<Music> out = new ArrayList<>();
        if (dto == null || dto.items == null) return out;

        for (PlaylistTrackItemDTO item : dto.items) {
            if (item == null || item.track == null) continue;
            SpotifyTrackDTO t = item.track;

            String artist = (t.artists != null && !t.artists.isEmpty())
                    ? t.artists.get(0).name
                    : "Desconhecido";
            String album = (t.album != null) ? t.album.name : null;
            String release = (t.album != null) ? t.album.releaseDate : null;
            String img = (t.album != null && t.album.images != null && !t.album.images.isEmpty())
                    ? t.album.images.get(0).url
                    : null;


            Music m = new Music(
                    t.name != null ? t.name : "",
                    artist,
                    img,
                    t.previewUrl,
                    album,
                    release,
                    t.uri
            );

            out.add(m);
        }

        return out;
    }

}
