package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.domain.model.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicMapper {
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
                    nz(it.uri)       // spotify:track:...
            ));
        }
        return out;
    }

    private static String nz(String s){ return s==null? "": s; }
}
