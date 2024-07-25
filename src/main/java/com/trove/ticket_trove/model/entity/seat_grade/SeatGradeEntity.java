package com.trove.ticket_trove.model.entity.seat_grade;


import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "seat_grade",
        indexes = {
            @Index(
                    name = "idx_seat_grade_concert_id_grade",
                    columnList = "concert_id, grade")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"concert_id","grade", "price", "deleted_at"})
)
@SQLDelete(sql = "UPDATE seat_grade " +
        "SET deleted_at = CURRENT_TIMESTAMP " +
        "WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatGradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id")
    private ConcertEntity concertId;
    private String grade;
    private Integer price;
    private Integer totalSeat;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
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

    public static SeatGradeEntity from(
            ConcertEntity concertEntity,
            String grade, Integer price, Integer totalSeat) {
        return SeatGradeEntity.builder()
                .concertId(concertEntity)
                .grade(grade)
                .price(price)
                .totalSeat(totalSeat)
                .build();
    }
}
