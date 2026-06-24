package com.musicapp.api.service;

import com.musicapp.api.domain.user.User;
import com.musicapp.api.domain.user.UserToken;
import com.musicapp.api.domain.user.UserToken.TokenProvider;
import com.musicapp.api.repository.UserRepository;
import com.musicapp.api.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public User findOrCreateUser(String spotifyId, String displayName, String email, String avatarUrl) {
        return userRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .spotifyId(spotifyId)
                                .displayName(displayName)
                                .email(email)
                                .avatarUrl(avatarUrl)
                                .build()
                ));
    }

    @Transactional
    public void saveToken(User user, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        UserToken token = userTokenRepository
                .findByUserIdAndProvider(user.getId(), TokenProvider.SPOTIFY)
                .orElse(UserToken.builder()
                        .user(user)
                        .provider(TokenProvider.SPOTIFY)
                        .build());

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(expiresAt);

        userTokenRepository.save(token);
    }

    public Optional<UserToken> getSpotifyToken(User user) {
        return userTokenRepository.findByUserIdAndProvider(user.getId(), TokenProvider.SPOTIFY);
    }
}