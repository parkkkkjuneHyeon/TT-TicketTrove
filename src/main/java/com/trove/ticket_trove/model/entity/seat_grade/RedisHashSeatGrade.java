package com.trove.ticket_trove.model.entity.seat_grade;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("SeatGrade")
public class RedisHashSeatGrade {
    @Id
    private String key;
    private Long id;
    private ConcertEntity concertId;
    private String grade;
    private Integer price;
    private Integer totalSeat;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static String ofKey(SeatGradeEntity seatGradeEntity) {
        return seatGradeEntity.getConcertId().getId()
                +seatGradeEntity.getGrade();
    }

    public static String ofKey(Long concertId, String grade) {
        return concertId+grade;
    }

    public static RedisHashSeatGrade from(SeatGradeEntity seatGradeEntity) {
        return new RedisHashSeatGrade(
                ofKey(seatGradeEntity),
                seatGradeEntity.getId(),
                seatGradeEntity.getConcertId(),
                seatGradeEntity.getGrade(),
                seatGradeEntity.getPrice(),
                seatGradeEntity.getTotalSeat(),
                seatGradeEntity.getCreatedAt(),
                seatGradeEntity.getUpdatedAt(),
                seatGradeEntity.getDeletedAt());
    }
    public static RedisHashSeatGrade from(
            SeatGradeEntity seatGradeEntity,
            ConcertEntity concertEntity) {
        return new RedisHashSeatGrade(
                ofKey(seatGradeEntity),
                seatGradeEntity.getId(),
                concertEntity,
                seatGradeEntity.getGrade(),
                seatGradeEntity.getPrice(),
                seatGradeEntity.getTotalSeat(),
                seatGradeEntity.getCreatedAt(),
                seatGradeEntity.getUpdatedAt(),
                seatGradeEntity.getDeletedAt());
    }
}
