package com.example.Apoloplay.domain.repository;

import com.example.Apoloplay.data.model.ShazamResponse;
import com.example.Apoloplay.utils.Result;
import java.io.File;

public interface ShazamRepository {
    Result<ShazamResponse> recognizeSong(File audioFile);
}