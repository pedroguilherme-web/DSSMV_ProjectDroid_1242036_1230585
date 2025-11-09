package com.example.Apoloplay.ui.main;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.Apoloplay.domain.usecase.RecognizeSongUseCase;

public class ShazamViewModelFactory implements ViewModelProvider.Factory {

    private final RecognizeSongUseCase recognizeSongUseCase;

    public ShazamViewModelFactory(RecognizeSongUseCase recognizeSongUseCase) {
        this.recognizeSongUseCase = recognizeSongUseCase;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ShazamViewModel.class)) {
            return (T) new ShazamViewModel(recognizeSongUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
    }
}