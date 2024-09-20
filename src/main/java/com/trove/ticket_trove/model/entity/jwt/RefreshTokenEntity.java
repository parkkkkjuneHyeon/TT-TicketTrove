package com.trove.ticket_trove.model.entity.jwt;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refreshtoken", columnList = "token"),
})
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;
    @Column(name = "token" , unique = true, nullable = false)
    private String refreshToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    private Long expirationTime;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        expirationTime = new Date().getTime() + 1000 * 60 * 30;
    }
}
