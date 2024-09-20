package com.trove.ticket_trove.service.concert.validation;

import com.trove.ticket_trove.exception.seatgrade.SeatGradeExistsException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;

public class SeatGradeValidation {


    public static void validateSeatGrade(
            ConcertEntity concertEntity,
            String grade,
            SeatGradeRepository seatGradeRepository) throws SeatGradeExistsException {
        seatGradeRepository.findByConcertIdAndGrade(
                        concertEntity,
                        grade.toUpperCase())
                .ifPresent(sg -> {throw new SeatGradeExistsException();}
                );
    }
}
