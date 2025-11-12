// app/src/main/java/com/example/Apoloplay/data/repository/ShazamRepositoryImpl.java
package com.example.Apoloplay.data.repository;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.example.Apoloplay.data.mapper.ShazamMapper;
import com.example.Apoloplay.data.remote.ShazamApiService;
import com.example.Apoloplay.data.remote.shazam.dto.ShazamResponseDTO;
import com.example.Apoloplay.domain.model.Music;
import com.example.Apoloplay.domain.repository.ShazamRepository;
import com.example.Apoloplay.utils.Constants;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ShazamRepositoryImpl implements ShazamRepository {

    private static final String TAG = "Shazam-Repo";
    private final ShazamApiService api;

    public ShazamRepositoryImpl(ShazamApiService api) {
        this.api = api;
    }

    @Override
    public Music recognize(File audioFile) throws Exception {
        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
            throw new IllegalArgumentException("Ficheiro de áudio inválido");
        }

        long durMs = -1L;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(audioFile.getAbsolutePath());
            String d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (d != null) durMs = Long.parseLong(d);
            mmr.release();
        } catch (Exception ignore) {}
        Log.d(TAG, "Audio duration(ms)=" + durMs + " size=" + audioFile.length());

        // >>> RAW body (alinha com ShazamApiService.recognizeSong(String, String, RequestBody))

        MediaType mt = MediaType.parse("application/octet-stream");
        RequestBody rb = RequestBody.create(mt, audioFile);


        long t0 = System.currentTimeMillis();
        Log.d(TAG, "Recognize request -> host=" + Constants.RAPID_API_HOST
                + " bytes=" + audioFile.length() + " mime=" + mt);

        Response<ShazamResponseDTO> resp = api
                .recognizeSong(Constants.RAPID_API_KEY, Constants.RAPID_API_HOST, rb)
                .execute();







        long dt = System.currentTimeMillis() - t0;
        Log.d(TAG, "HTTP " + resp.code() + " in " + dt + "ms");
        Log.d(TAG, "Resp headers: " + resp.headers());

        if (!resp.isSuccessful() || resp.body() == null) {
            String err = (resp.errorBody() != null) ? resp.errorBody().string() : "sem detalhe";
            if (resp.code() == 403 && err != null && err.contains("not subscribed")) {
                throw new RuntimeException("API não subscrita no RapidAPI (403). Verifica Application/Key.");
            }
            if (resp.code() == 415) {
                throw new RuntimeException("Tipo de ficheiro não aceite (415). Tenta outro formato (m4a/mp3) ou confirma 'application/octet-stream'.");
            }
            throw new RuntimeException("Reconhecimento falhou: HTTP " + resp.code() + " - " + err);
        }

        ShazamResponseDTO body = resp.body();
        Music m = ShazamMapper.toDomain(body);
        if (m == null || m.getTitle() == null) throw new RuntimeException("Música não reconhecida");
        Log.i(TAG, "Reconhecido: " + m.getTitle() + " · " + m.getArtist());
        return m;
    }









}
