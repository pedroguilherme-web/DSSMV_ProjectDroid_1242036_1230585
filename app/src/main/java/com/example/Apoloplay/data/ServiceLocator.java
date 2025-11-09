package com.example.Apoloplay.data;

import com.example.Apoloplay.data.auth.SpotifySessionProvider;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.repository.PlaylistsRepositoryImpl;
import com.example.Apoloplay.domain.repository.PlaylistsRepository;
import com.example.Apoloplay.data.repository.ShazamRepositoryImpl;
import com.example.Apoloplay.data.remote.ShazamApiService;
import com.example.Apoloplay.domain.repository.ShazamRepository;
import com.example.Apoloplay.domain.usecase.RecognizeSongUseCase;
import com.example.Apoloplay.ui.main.ShazamViewModelFactory;
import retrofit2.Retrofit;

public final class ServiceLocator {

    private static ServiceLocator instance;
    private static SpotifyService spotifyService;
    private static SpotifySessionProvider sessionProvider;
    private static PlaylistsRepository playlistsRepository;

    private ServiceLocator(){}

    public static ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    private ShazamApiService provideShazamApiService() {
        return RetrofitProvider.provideShazamRetrofit().create(ShazamApiService.class);
    }

    private ShazamRepository provideShazamRepository() {
        return new ShazamRepositoryImpl(provideShazamApiService());
    }

    private RecognizeSongUseCase provideRecognizeSongUseCase() {
        return new RecognizeSongUseCase(provideShazamRepository());
    }

    public ShazamViewModelFactory provideShazamViewModelFactory() {
        return new ShazamViewModelFactory(provideRecognizeSongUseCase());
    }

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