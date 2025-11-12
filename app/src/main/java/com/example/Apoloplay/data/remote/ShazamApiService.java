package com.example.Apoloplay.data.remote;

import com.example.Apoloplay.data.remote.shazam.dto.ShazamResponseDTO;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ShazamApiService {


    @POST("recognize/file")
    Call<ShazamResponseDTO> recognizeSong(
            @Header("X-RapidAPI-Key") String apiKey,
            @Header("X-RapidAPI-Host") String apiHost,
            @Body RequestBody fileBytes
    );
}
