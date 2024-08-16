package com.trove.ticket_trove.model.entity.ticket;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.lang.reflect.Member;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash(value = "Ticket")
public class RedisHashTicket {
    @Id
    private String key;

    private Long id;
    private MemberEntity memberEmail;
    private ConcertEntity concertId;
    private SeatGradeEntity seatGrade;
    private Integer seatNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static String ofKey(TicketEntity ticketEntity) {
        return ticketEntity.getMemberEmail().getEmail()
                +ticketEntity.getConcertId().getId()
                +ticketEntity.getSeatGrade().getGrade()
                +ticketEntity.getSeatNumber();
    }
    public static String ofKey(
            String email, long concertId, String grade, Integer seatNumber) {
        return email+concertId+grade+seatNumber;
    }

    public static RedisHashTicket from(
            TicketEntity ticketEntity, MemberEntity memberEntity,
            ConcertEntity concertEntity, SeatGradeEntity seatGradeEntity) {
        return new RedisHashTicket(ofKey(ticketEntity),
                ticketEntity.getId(),
                memberEntity,
                concertEntity,
                seatGradeEntity,
                ticketEntity.getSeatNumber(),
                ticketEntity.getCreatedAt(),
                ticketEntity.getUpdatedAt(),
                ticketEntity.getDeletedAt());
    }

}
