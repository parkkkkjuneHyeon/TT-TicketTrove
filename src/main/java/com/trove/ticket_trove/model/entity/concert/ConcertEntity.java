package com.trove.ticket_trove.model.entity.concert;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "concert",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"concert_name", "performer","show_start", "deleted_at"})
})
@SQLDelete(sql = "UPDATE concert " +
        "SET deleted_at = CURRENT_TIMESTAMP " +
        "WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
public class ConcertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "concert_name")
    private String concertName;
    private String performer;
    private LocalDateTime showStart;
    private LocalDateTime showEnd;
    private LocalDateTime ticketingTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static ConcertEntity to(RedisHashConcert redisHashConcert) {
        return ConcertEntity.builder()
                .id(redisHashConcert.getId())
                .concertName(redisHashConcert.getConcertName())
                .performer(redisHashConcert.getPerformer())
                .showStart(redisHashConcert.getShowStart())
                .showEnd(redisHashConcert.getShowEnd())
                .ticketingTime(redisHashConcert.getTicketingTime())
                .createdAt(redisHashConcert.getCreatedAt())
                .updatedAt(redisHashConcert.getUpdatedAt())
                .deletedAt(redisHashConcert.getDeletedAt())
                .build();
    }

}
