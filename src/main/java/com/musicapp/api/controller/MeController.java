package com.musicapp.api.controller;

import com.musicapp.api.domain.user.User;
import com.musicapp.api.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final SyncService syncService;

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncSpotify(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "medium_term") String timeRange) {

        SyncService.SyncResult result = syncService.syncSpotifyData(user, timeRange);

        return ResponseEntity.ok(Map.of(
                "message", "Sincronização concluída",
                "source", result.source(),
                "syncedCount", result.syncedCount()
        ));
    }

    @PostMapping("/lastfm")
    public ResponseEntity<Map<String, Object>> connectLastFm(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {

        String lastFmUsername = body.get("username");
        if (lastFmUsername == null || lastFmUsername.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username do Last.fm é obrigatório"
            ));
        }

        SyncService.SyncResult result = syncService.syncLastFmData(user, lastFmUsername.trim());

        return ResponseEntity.ok(Map.of(
                "message", "Last.fm conectado e dados sincronizados",
                "source", result.source(),
                "syncedCount", result.syncedCount()
        ));
    }
}
