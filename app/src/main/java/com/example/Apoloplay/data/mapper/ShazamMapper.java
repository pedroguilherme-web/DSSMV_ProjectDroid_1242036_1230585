// data/mapper/ShazamMapper.java
package com.example.Apoloplay.data.mapper;

import com.example.Apoloplay.data.remote.shazam.dto.ShazamResponseDTO;
import com.example.Apoloplay.data.remote.shazam.dto.TrackDTO;
import com.example.Apoloplay.domain.model.Music;

public class ShazamMapper {

    public static Music toDomain(ShazamResponseDTO dto) {
        if (dto == null) return null;

        com.example.Apoloplay.data.remote.shazam.dto.TrackDTO t = dto.track;
        if (t == null && dto.matches != null && !dto.matches.isEmpty()) {
            t = dto.matches.get(0).track; // fallback
        }
        if (t == null) return null;

        String title  = t.getTitle();
        String artist = t.getSubtitle();
        com.example.Apoloplay.data.remote.shazam.dto.TrackDTO.Images imgs = t.getImages();
        String cover = (imgs != null) ? imgs.getCoverart() : null;

        return new Music(title, artist, cover, null, null, null, null);
    }

}
