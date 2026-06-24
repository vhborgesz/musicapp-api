package com.musicapp.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.musicapp.api.domain.user.User;
import com.musicapp.api.service.JwtService;
import com.musicapp.api.service.SpotifyAuthService;
import com.musicapp.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SpotifyAuthService spotifyAuthService;
    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/spotify/url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String url = spotifyAuthService.buildAuthorizationUrl();
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/spotify/callback")
    public ResponseEntity<Map<String, String>> handleCallback(@RequestParam String code) {
        JsonNode tokens = spotifyAuthService.exchangeCodeForTokens(code);

        String accessToken = tokens.get("access_token").asText();
        String refreshToken = tokens.has("refresh_token")
                ? tokens.get("refresh_token").asText()
                : null;
        int expiresIn = tokens.get("expires_in").asInt();

        JsonNode userInfo = spotifyAuthService.getUserInfo(accessToken);

        String spotifyId = userInfo.get("id").asText();
        String displayName = userInfo.has("display_name")
                ? userInfo.get("display_name").asText()
                : spotifyId;
        String email = userInfo.has("email")
                ? userInfo.get("email").asText()
                : null;
        String avatarUrl = extractAvatarUrl(userInfo);

        User user = userService.findOrCreateUser(spotifyId, displayName, email, avatarUrl);

        userService.saveToken(
                user,
                accessToken,
                refreshToken,
                LocalDateTime.now().plusSeconds(expiresIn)
        );

        String jwt = jwtService.generateToken(user.getId(), spotifyId);

        return ResponseEntity.ok(Map.of("token", jwt));
    }

    private String extractAvatarUrl(JsonNode userInfo) {
        try {
            JsonNode images = userInfo.get("images");
            if (images != null && images.isArray() && images.size() > 0) {
                return images.get(0).get("url").asText();
            }
        } catch (Exception ignored) {}
        return null;
    }
}