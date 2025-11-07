package com.example.Apoloplay.data.auth;

public interface SessionProvider {
    String getUserAccessToken();
    void setUserAccessToken(String token);
}
