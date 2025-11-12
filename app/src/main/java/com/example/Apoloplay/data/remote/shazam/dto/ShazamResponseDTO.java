package com.example.Apoloplay.data.remote.shazam.dto;

import java.util.List;

public class ShazamResponseDTO {
    // alguns responses trazem o track no topo
    public TrackDTO track;

    // outros trazem numa lista de matches
    public List<MatchDTO> matches;

    public static class MatchDTO {
        public TrackDTO track;
    }
}
