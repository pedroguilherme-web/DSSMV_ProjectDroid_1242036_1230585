package com.example.Apoloplay.data.repository;

import com.example.Apoloplay.data.model.ShazamResponse;
import com.example.Apoloplay.data.remote.ShazamApiService;
import com.example.Apoloplay.domain.repository.ShazamRepository;
import com.example.Apoloplay.utils.Constants;
import com.example.Apoloplay.utils.Result;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class ShazamRepositoryImpl implements ShazamRepository {
    private final ShazamApiService apiService;
    public ShazamRepositoryImpl(ShazamApiService apiService) {
        this.apiService = apiService;
    }
    @Override
    public Result<ShazamResponse> recognizeSong(File audioFile) {
        try {
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("audio/wav"),
                    audioFile
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestFile);

            Response<ShazamResponse> response = apiService.recognizeSong(
                    Constants.RAPID_API_KEY,
                    Constants.RAPID_API_HOST,
                    body
            ).execute(); // Chamada síncrona, executada no Executor

            if (response.isSuccessful() && response.body() != null && response.body().getTrack() != null) {
                return new Result.Success<>(response.body());
            } else {
                return new Result.Error<>(new Exception("Música não reconhecida. Code: " + response.code()));
            }
        } catch (Exception e) {
            return new Result.Error<>(e);
        }
    }
}