package com.trove.ticket_trove.model.entity.concert;


import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
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

    public static ConcertEntity from(ConcertDetailsInfoResponse concertDetailsInfoResponse){
        return ConcertEntity.builder()
                .id(concertDetailsInfoResponse.concertId())
                .concertName(concertDetailsInfoResponse.concertName())
                .performer(concertDetailsInfoResponse.performer())
                .showStart(concertDetailsInfoResponse.showStart())
                .showEnd(concertDetailsInfoResponse.showEnd())
                .ticketingTime(concertDetailsInfoResponse.ticketingTime())
                .build();


    }




}
