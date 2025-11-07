package com.example.Apoloplay.Model;


import java.util.List;

public class Playlist {
    public String id;
    public String name;
    public List<Image> images;

    public static class Image {
        public String url;
        public Integer width;
        public Integer height;
    }
}
