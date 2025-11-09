package com.example.Apoloplay.data;

import com.example.Apoloplay.data.auth.SpotifySessionProvider;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.repository.PlaylistsRepositoryImpl;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;

public final class ServiceLocator {

    private static SpotifyService spotifyService;
    private static SpotifySessionProvider sessionProvider;
    private static PlaylistsRepository playlistsRepository;

    private ServiceLocator(){}

    public static SpotifyService spotifyService() {
        if (spotifyService == null) spotifyService = RetrofitProvider.provideSpotifyService();
        return spotifyService;
    }

    public static SpotifySessionProvider sessionProvider() {
        if (sessionProvider == null) sessionProvider = new SpotifySessionProvider();
        return sessionProvider;
    }

    public static PlaylistsRepository playlistsRepository() {
        if (playlistsRepository == null) {
            playlistsRepository = new PlaylistsRepositoryImpl(spotifyService(), sessionProvider());
        }
        return playlistsRepository;
    }
}
