package com.example.Apoloplay.domain.usecase;

import com.example.Apoloplay.data.model.ShazamResponse;
import com.example.Apoloplay.domain.repository.ShazamRepository;
import com.example.Apoloplay.utils.Result;
import java.io.File;

public class RecognizeSongUseCase {

    private final ShazamRepository repository;

    public RecognizeSongUseCase(ShazamRepository repository) {
        this.repository = repository;
    }

    public Result<ShazamResponse> execute(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            return new Result.Error<>(new IllegalArgumentException("Arquivo de áudio não reconhecido."));
        }

        return repository.recognizeSong(audioFile);
    }
}