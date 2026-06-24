package com.musicapp.api.repository;

import com.musicapp.api.domain.user.UserToken;
import com.musicapp.api.domain.user.UserToken.TokenProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByUserIdAndProvider(UUID userId, TokenProvider provider);

}
