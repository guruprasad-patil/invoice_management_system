package com.gsp.aiims.auth.service;

import com.gsp.aiims.auth.dto.LoginRequest;
import com.gsp.aiims.auth.dto.LoginResponse;
import com.gsp.aiims.auth.dto.RefreshTokenRequest;
import com.gsp.aiims.auth.dto.RegisterRequest;
import com.gsp.aiims.auth.entity.RefreshToken;
import com.gsp.aiims.auth.entity.User;
import com.gsp.aiims.auth.repository.RefreshTokenRepository;
import com.gsp.aiims.auth.repository.UserRepository;
import com.gsp.aiims.auth.security.JwtTokenProvider;
import com.gsp.aiims.auth.security.UserPrincipal;
import com.gsp.aiims.common.exception.DuplicateResourceException;
import com.gsp.aiims.common.exception.ResourceNotFoundException;
import com.gsp.aiims.common.exception.TokenException;
import com.gsp.aiims.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String accessToken = jwtTokenProvider.generateToken(principal);
        RefreshToken refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildLoginResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .enabled(true)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        UserPrincipal principal = UserPrincipal.fromUser(user);
        String accessToken = jwtTokenProvider.generateToken(principal);
        RefreshToken refreshToken = createRefreshToken(user);

        return buildLoginResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (stored.isRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new TokenException("Refresh token has expired. Please login again");
        }

        User user = stored.getUser();
        UserPrincipal principal = UserPrincipal.fromUser(user);
        String newAccessToken = jwtTokenProvider.generateToken(principal);

        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return buildLoginResponse(newAccessToken, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("User logged out, token revoked for user: {}", token.getUser().getEmail());
        });
    }

    private RefreshToken createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID() + "-" + UUID.randomUUID();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshExpirationMs() / 1000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    private LoginResponse buildLoginResponse(String accessToken, RefreshToken refreshToken, User user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpirationMs() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
