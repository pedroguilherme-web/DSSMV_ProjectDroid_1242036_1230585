package com.example.Apoloplay.data.repository.interfaces;

import com.example.Apoloplay.data.model.Music;
import java.io.File;

public interface ShazamRepository {
    Music recognize(File audioFile) throws Exception;  // <- devolve Music
}
