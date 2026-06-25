package com.musicapp.api.domain.stats;

import com.musicapp.api.domain.artist.Artist;
import com.musicapp.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_artist_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserArtistStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Column(name = "play_count")
    private Integer playCount;

    @Column(name = "spotify_rank")
    private Integer spotifyRank;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private DataSource source;

    public enum DataSource {
        SPOTIFY, LASTFM
    }
}
