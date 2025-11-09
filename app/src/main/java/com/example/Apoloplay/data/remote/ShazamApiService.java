package com.example.Apoloplay.data.remote;

import com.example.Apoloplay.data.model.ShazamResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ShazamApiService {

    public static final String HOST = "shazam-core.p.rapidapi.com";

    String ENDPOINT = "v1/tracks/recognize";

    @Multipart
    @POST(ENDPOINT)
    Call<ShazamResponse> recognizeSong(

            @Header("X-RapidAPI-Key") String apiKey,
            @Header("X-RapidAPI-Host") String apiHost,
            @Part MultipartBody.Part audioFile
    );
}