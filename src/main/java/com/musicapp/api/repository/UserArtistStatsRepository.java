package com.musicapp.api.repository;

import com.musicapp.api.domain.stats.UserArtistStats;
import com.musicapp.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserArtistStatsRepository extends JpaRepository<UserArtistStats, UUID> {

    List<UserArtistStats> findByUser(User user);

    Optional<UserArtistStats> findByUserAndArtist(User user, com.musicapp.api.domain.artist.Artist artist);

    List<UserArtistStats> findByUserIdAndSource(UUID userId, UserArtistStats.DataSource source);
}
