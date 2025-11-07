// com/example/Apoloplay/data/auth/SpotifySessionProvider.java
package com.example.Apoloplay.data.auth;

public class SpotifySessionProvider implements SessionProvider {
    private volatile String userAccessToken;

    @Override
    public String getUserAccessToken() {
        return userAccessToken;
    }

    // chama isto depois do login
    public void setUserAccessToken(String token) {
        this.userAccessToken = token;
    }
}
