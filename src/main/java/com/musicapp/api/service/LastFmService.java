package com.musicapp.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LastFmService {

    @Value("${lastfm.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<LastFmArtist> getTopArtists(String username, int limit) {
        if (!isConfigured()) {
            throw new IllegalStateException("Last.fm API key não configurada");
        }

        String url = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("method", "user.gettopartists")
                .queryParam("user", username)
                .queryParam("limit", limit)
                .queryParam("format", "json")
                .queryParam("api_key", apiKey)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode topartists = root.path("topartists");
            JsonNode artistArray = topartists.path("artist");

            List<LastFmArtist> artists = new ArrayList<>();
            if (artistArray.isArray()) {
                int rank = 1;
                for (JsonNode artistNode : artistArray) {
                    String name = artistNode.path("name").asText();
                    int playcount = artistNode.path("playcount").asInt();
                    String mbid = artistNode.has("mbid") ? artistNode.path("mbid").asText() : null;

                    List<String> images = new ArrayList<>();
                    JsonNode imageArray = artistNode.path("image");
                    if (imageArray.isArray()) {
                        for (JsonNode img : imageArray) {
                            String size = img.path("size").asText();
                            String text = img.path("#text").asText();
                            if ("extralarge".equals(size) || "large".equals(size)) {
                                if (text != null && !text.isEmpty()) {
                                    images.add(text);
                                }
                            }
                        }
                    }

                    artists.add(new LastFmArtist(name, playcount, mbid, images.isEmpty() ? null : images.get(0), rank));
                    rank++;
                }
            }
            return artists;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar resposta do Last.fm", e);
        }
    }

    public record LastFmArtist(String name, int playcount, String mbid, String imageUrl, int rank) {}
}
