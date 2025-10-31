package com.example.Apoloplay.Model.auth.service;



import com.example.Apoloplay.Model.auth.model.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Interface Retrofit para o endpoint de Autorização (Obtenção do Token)
 */
public interface AuthTokenService {

    @FormUrlEncoded
    @POST("api/token")
    Call<TokenResponse> getAccessToken(
            @Header("Authorization") String authorization,
            @Field("grant_type") String grantType
    );
}