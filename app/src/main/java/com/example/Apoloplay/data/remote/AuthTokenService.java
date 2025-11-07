package com.example.Apoloplay.data.remote;

import com.example.Apoloplay.data.remote.dto.TokenResponseDTO;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthTokenService {
    @FormUrlEncoded
    @POST("api/token")
    Call<TokenResponseDTO> getAccessToken(
            @Header("Authorization") String basicAuth,
            @Field("grant_type") String grantType
    );
}
