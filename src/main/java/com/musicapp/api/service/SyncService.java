package com.musicapp.api.service;

import com.musicapp.api.domain.artist.Artist;
import com.musicapp.api.domain.stats.UserArtistStats;
import com.musicapp.api.domain.user.User;
import com.musicapp.api.domain.user.UserToken;
import com.musicapp.api.repository.ArtistRepository;
import com.musicapp.api.repository.UserArtistStatsRepository;
import com.musicapp.api.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final SpotifyDataService spotifyDataService;
    private final LastFmService lastFmService;
    private final ArtistRepository artistRepository;
    private final UserArtistStatsRepository userArtistStatsRepository;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public SyncResult syncSpotifyData(User user, String timeRange) {
        UserToken token = userTokenRepository
                .findByUserIdAndProvider(user.getId(), UserToken.TokenProvider.SPOTIFY)
                .orElseThrow(() -> new IllegalStateException("Token do Spotify não encontrado"));

        List<SpotifyDataService.SpotifyArtist> spotifyArtists =
                spotifyDataService.getTopArtists(token.getAccessToken(), 50, timeRange);

        int synced = 0;
        for (SpotifyDataService.SpotifyArtist sa : spotifyArtists) {
            Artist artist = findOrCreateArtist(sa.spotifyId(), sa.name(), sa.imageUrl());

            UserArtistStats stats = userArtistStatsRepository
                    .findByUserAndArtist(user, artist)
                    .orElse(UserArtistStats.builder()
                            .user(user)
                            .artist(artist)
                            .source(UserArtistStats.DataSource.SPOTIFY)
                            .build());

            stats.setSpotifyRank(sa.rank());
            stats.setLastSyncedAt(LocalDateTime.now());

            userArtistStatsRepository.save(stats);
            synced++;
        }

        return new SyncResult("SPOTIFY", synced);
    }

    @Transactional
    public SyncResult syncLastFmData(User user, String lastFmUsername) {
        if (!lastFmService.isConfigured()) {
            throw new IllegalStateException("Serviço do Last.fm não configurado");
        }

        List<LastFmService.LastFmArtist> lastFmArtists =
                lastFmService.getTopArtists(lastFmUsername, 50);

        int synced = 0;
        for (LastFmService.LastFmArtist lfa : lastFmArtists) {
            Artist artist = findOrCreateArtist(null, lfa.name(), lfa.imageUrl());
            if (lfa.mbid() != null && artist.getLastfmId() == null) {
                artist.setLastfmId(lfa.mbid());
                artistRepository.save(artist);
            }

            UserArtistStats stats = userArtistStatsRepository
                    .findByUserAndArtist(user, artist)
                    .orElse(UserArtistStats.builder()
                            .user(user)
                            .artist(artist)
                            .source(UserArtistStats.DataSource.LASTFM)
                            .build());

            stats.setPlayCount(lfa.playcount());
            stats.setLastSyncedAt(LocalDateTime.now());

            userArtistStatsRepository.save(stats);
            synced++;
        }

        return new SyncResult("LASTFM", synced);
    }

    private Artist findOrCreateArtist(String spotifyId, String name, String imageUrl) {
        Optional<Artist> existing = Optional.empty();

        if (spotifyId != null) {
            existing = artistRepository.findBySpotifyId(spotifyId);
        }

        return existing.orElseGet(() -> artistRepository.save(
                Artist.builder()
                        .spotifyId(spotifyId)
                        .name(name)
                        .imageUrl(imageUrl)
                        .build()
        ));
    }

    public record SyncResult(String source, int syncedCount) {}
}
