package com.gsp.aiims.auth.repository;

import com.gsp.aiims.auth.entity.RefreshToken;
import com.gsp.aiims.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllUserTokens(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokens(LocalDateTime now);
}
