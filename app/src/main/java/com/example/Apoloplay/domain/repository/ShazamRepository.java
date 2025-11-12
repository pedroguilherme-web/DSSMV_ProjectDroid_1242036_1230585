package com.example.Apoloplay.domain.repository;

import com.example.Apoloplay.domain.model.Music;
import java.io.File;

public interface ShazamRepository {
    Music recognize(File audioFile) throws Exception;  // <- devolve Music
}
