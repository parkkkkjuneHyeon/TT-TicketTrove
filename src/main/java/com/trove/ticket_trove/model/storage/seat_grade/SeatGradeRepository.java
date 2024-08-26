package com.trove.ticket_trove.model.storage.seat_grade;

import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatGradeRepository extends JpaRepository<SeatGradeEntity, Long> {
    Optional<SeatGradeEntity> findByConcertIdAndGrade(ConcertEntity concertEntity, String grade);

    void deleteAllByConcertId(ConcertEntity concertEntity);

    List<SeatGradeEntity> findByConcertIdOrderByPriceDesc(ConcertEntity concertEntity);

    Optional<SeatGradeEntity> findByConcertIdAndGradeAndPrice(ConcertEntity concertEntity, String grade, Integer price);

    void deleteByConcertIdAndGradeAndPrice(ConcertEntity concertEntity, String grade, Integer price);

    List<SeatGradeEntity> findByConcertId(ConcertEntity concertEntity);
}
