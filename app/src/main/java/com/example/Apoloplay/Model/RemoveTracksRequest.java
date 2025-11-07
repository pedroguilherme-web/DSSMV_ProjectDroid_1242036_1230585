package com.example.Apoloplay.Model;



import java.util.*;

public class RemoveTracksRequest {
    public List<Map<String, String>> tracks;

    public RemoveTracksRequest(String uri) {
        Map<String, String> m = new HashMap<>();
        m.put("uri", uri);
        this.tracks = Collections.singletonList(m);
    }
}
