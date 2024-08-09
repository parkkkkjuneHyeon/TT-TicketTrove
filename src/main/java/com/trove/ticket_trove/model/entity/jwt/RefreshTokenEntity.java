package com.trove.ticket_trove.model.entity.jwt;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;
    @Column(name = "token" , unique = true, nullable = false)
    private String refreshToken;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    private Long expirationTime;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        expirationTime = new Date().getTime() + 1000 * 60 * 24 * 7;
//                .plusDays(7); // 7일 후 자동 만료
    }
}
