package com.example.Apoloplay.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.Apoloplay.data.model.ShazamResponse;
import com.example.Apoloplay.domain.usecase.RecognizeSongUseCase;
import com.example.Apoloplay.utils.Result;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ShazamViewModel extends ViewModel {

    private final RecognizeSongUseCase recognizeSongUseCase;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Result<ShazamResponse>> _recognitionResult = new MutableLiveData<>();
    public LiveData<Result<ShazamResponse>> getRecognitionResult() { return _recognitionResult; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    public ShazamViewModel(RecognizeSongUseCase recognizeSongUseCase) {
        this.recognizeSongUseCase = recognizeSongUseCase;
    }

    public void startRecognition(File audioFile) {
        _isLoading.setValue(true);
        _recognitionResult.setValue(null);

        executor.execute(() -> {
            Result<ShazamResponse> result = recognizeSongUseCase.execute(audioFile);
            _recognitionResult.postValue(result);
            _isLoading.postValue(false);
        });
    }
}
