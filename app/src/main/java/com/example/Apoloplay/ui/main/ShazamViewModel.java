package com.example.Apoloplay.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.ShazamRepository;
import com.example.Apoloplay.ui.shazam.ShazamUiState;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShazamViewModel extends ViewModel {

    private final ShazamRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final MutableLiveData<ShazamUiState> _state =
            new MutableLiveData<>(ShazamUiState.idle());
    public LiveData<ShazamUiState> getState() { return _state; }

    public ShazamViewModel() {
        this.repo = ServiceLocator.shazamRepository();
    }

    /** chamado quando o botão “Shazam” passa para gravar */
    public void startRecording() {
        _state.postValue(ShazamUiState.recording());
    }

    /** envia o ficheiro gravado para reconhecimento */
    public void startRecognition(File audioFile) {
        if (running.get()) return;
        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
            _state.postValue(ShazamUiState.error("Ficheiro de áudio inválido"));
            return;
        }

        running.set(true);
        _state.postValue(ShazamUiState.loading());

        executor.execute(() -> {
            try {
                Music music = repo.recognize(audioFile);
                _state.postValue(ShazamUiState.data(music));
            } catch (Exception e) {
                _state.postValue(ShazamUiState.error(
                        (e.getMessage() != null && !e.getMessage().isEmpty())
                                ? e.getMessage()
                                : "Erro desconhecido no reconhecimento"));
            } finally {
                running.set(false);
            }
        });
    }


    public void cancel() {
        _state.postValue(ShazamUiState.idle());
        running.set(false);

    }

    @Override protected void onCleared() {
        executor.shutdownNow();
        running.set(false);
        super.onCleared();
    }
}
