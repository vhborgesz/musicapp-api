package com.musicapp.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotifyDataService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOP_ARTISTS_URL = "https://api.spotify.com/v1/me/top/artists";

    public List<SpotifyArtist> getTopArtists(String accessToken, int limit, String timeRange) {
        String url = TOP_ARTISTS_URL + "?limit=" + limit + "&time_range=" + timeRange;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            List<SpotifyArtist> artists = new ArrayList<>();
            if (items.isArray()) {
                int rank = 1;
                for (JsonNode item : items) {
                    String id = item.path("id").asText();
                    String name = item.path("name").asText();

                    String imageUrl = null;
                    JsonNode images = item.path("images");
                    if (images.isArray() && !images.isEmpty()) {
                        imageUrl = images.get(0).path("url").asText();
                    }

                    artists.add(new SpotifyArtist(id, name, imageUrl, rank));
                    rank++;
                }
            }
            return artists;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta do Spotify", e);
        }
    }

    public record SpotifyArtist(String spotifyId, String name, String imageUrl, int rank) {}
}
