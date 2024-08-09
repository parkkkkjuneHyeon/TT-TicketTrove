package com.trove.ticket_trove.model.storage.jwt;

import com.trove.ticket_trove.model.entity.jwt.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_token " +
            "where expiration_time < :currentMillis", nativeQuery = true)
    void deleteExpiredToken(@Param("currentMillis") long currentMillis);
}
