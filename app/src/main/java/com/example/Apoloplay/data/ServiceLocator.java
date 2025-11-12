// app/src/main/java/com/example/Apoloplay/data/ServiceLocator.java
package com.example.Apoloplay.data;

import com.example.Apoloplay.data.auth.SpotifySessionProvider;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.ShazamApiService;   // <- pacote certo
import com.example.Apoloplay.data.repository.PlaylistsRepositoryImpl;
import com.example.Apoloplay.data.repository.ShazamRepositoryImpl;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;
import com.example.Apoloplay.domain.repository.ShazamRepository;


public final class ServiceLocator {

    private static ServiceLocator instance;

    // Spotify
    private static SpotifyService spotifyService;
    private static SpotifySessionProvider sessionProvider;
    private static PlaylistsRepository playlistsRepository;

    // Shazam
    private static ShazamApiService shazamApiService;
    private static ShazamRepository shazamRepository;


    private ServiceLocator() {}

    public static ServiceLocator getInstance() {
        if (instance == null) instance = new ServiceLocator();
        return instance;
    }

    // -------- Spotify --------
    public static SpotifyService spotifyService() {
        if (spotifyService == null) {
            spotifyService = RetrofitProvider.provideSpotifyService();
        }
        return spotifyService;
    }

    public static SpotifySessionProvider sessionProvider() {
        if (sessionProvider == null) {
            sessionProvider = new SpotifySessionProvider();
        }
        return sessionProvider;
    }

    public static PlaylistsRepository playlistsRepository() {
        if (playlistsRepository == null) {
            playlistsRepository = new PlaylistsRepositoryImpl(spotifyService(), sessionProvider());
        }
        return playlistsRepository;
    }


    // -------- Shazam --------
    public static ShazamApiService shazamApiService() {
        if (shazamApiService == null) {
            shazamApiService = RetrofitProvider
                    .provideShazamRetrofit()
                    .create(ShazamApiService.class);
        }
        return shazamApiService;
    }

    public static ShazamRepository shazamRepository() {
        if (shazamRepository == null) {
            shazamRepository = new ShazamRepositoryImpl(shazamApiService());
        }
        return shazamRepository;
    }


}

