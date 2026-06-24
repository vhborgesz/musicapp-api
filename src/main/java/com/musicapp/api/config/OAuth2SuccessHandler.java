package com.musicapp.api.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.musicapp.api.domain.user.User;
import com.musicapp.api.service.JwtService;
import com.musicapp.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 callback recebido para usuário: {}", authentication.getName());
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String spotifyId = oAuth2User.getAttribute("id");
        String displayName = oAuth2User.getAttribute("display_name");
        String email = oAuth2User.getAttribute("email");
        String avatarUrl = extractAvatarUrl(oAuth2User);

        User user = userService.findOrCreateUser(spotifyId, displayName, email, avatarUrl);

        String accessToken = (String) oAuth2User.getAttribute("access_token");
        String refreshToken = (String) oAuth2User.getAttribute("refresh_token");

        userService.saveToken(user,
                accessToken != null ? accessToken : "pending",
                refreshToken,
                LocalDateTime.now().plusHours(1)
        );

        String jwt = jwtService.generateToken(user.getId(), spotifyId);

        getRedirectStrategy().sendRedirect(request, response,
                "http://localhost:3000?token=" + jwt);
    }

    private String extractAvatarUrl(OAuth2User oAuth2User) {
        try {
            var images = oAuth2User.<java.util.List<java.util.Map<String, Object>>>getAttribute("images");
            if (images != null && !images.isEmpty()) {
                return (String) images.get(0).get("url");
            }
        } catch (Exception ignored) {}
        return null;
    }
}