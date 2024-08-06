package com.trove.ticket_trove.model.storage.ticket;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    @Query("SELECT t " +
            "FROM TicketEntity t " +
            "WHERE t.concertId = :concertId " +
                "AND t.seatGrade = :seatGrade " +
                "AND t.memberEmail = :memberEmail " +
                "AND t.seatNumber = :seatNumber")
    Optional<TicketEntity> findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
            @Param("concertId") ConcertEntity concertId,
            @Param("seatGrade") SeatGradeEntity seatGrade,
            @Param("memberEmail") MemberEntity memberEmail,
            @Param("seatNumber") Integer seatNumber);

    List<TicketEntity> findByMemberEmailOrderByCreatedAtAsc(MemberEntity memberEntity, Pageable pageable);

    List<TicketEntity> findByConcertIdOrderByCreatedAtAsc(ConcertEntity concertId, Pageable pageable);
}
