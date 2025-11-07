package com.example.Apoloplay.data.remote.dto;

import java.util.List;

public class RemoveTracksRequest {

    private List<TrackUri> tracks;

    public RemoveTracksRequest(String trackUri) {
        this.tracks = List.of(new TrackUri(trackUri));
    }

    public List<TrackUri> getTracks() {
        return tracks;
    }

    public static class TrackUri {
        private String uri;

        public TrackUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }
    }
}
