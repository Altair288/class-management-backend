package com.altair288.class_management.repository;

import com.altair288.class_management.model.PasswordResetToken;
import com.altair288.class_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.used = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findActiveTokensForUser(@Param("user") User user, @Param("now") Date now);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.requestIp = :ip AND t.createdAt > :since")
    long countRecentRequestsFromIp(@Param("ip") String ip, @Param("since") Date since);
}
