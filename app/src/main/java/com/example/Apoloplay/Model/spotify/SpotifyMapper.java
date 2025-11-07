package com.example.Apoloplay.Model.spotify;



import com.example.Apoloplay.Model.Music;


import java.util.ArrayList;
import java.util.List;

public class SpotifyMapper {


    public static List<Music> mapResponseToMusic(SpotifyResponse response) {
        List<Music> musicList = new ArrayList<>();
        if (response.getTracks() != null && response.getTracks().getItems() != null) {
            for (SpotifyResponse.TrackItem item : response.getTracks().getItems()) {
                String title = item.getName();
                String artist = item.getArtists().isEmpty()
                        ? "Artista Desconhecido"
                        : item.getArtists().get(0).getName();

                String imageUrl = (item.getAlbum() != null &&
                        item.getAlbum().getImages() != null &&
                        !item.getAlbum().getImages().isEmpty())
                        ? item.getAlbum().getImages().get(0).getUrl()
                        : null;

                String trackUri = item.getUri();
                String albumName = (item.getAlbum() != null) ? item.getAlbum().getName() : null;
                String releaseDate = (item.getAlbum() != null) ? item.getAlbum().getReleaseDate() : null;
                String previewUrl = item.getPreviewUrl();

                musicList.add(new Music(title, artist, imageUrl, trackUri, albumName, releaseDate, previewUrl));
            }
        }
        return musicList;
    }
}
