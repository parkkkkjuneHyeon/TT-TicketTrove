package com.trove.ticket_trove.dto.concert.response;

import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertDetailsInfoResponse(
        Long concertId,
        String concertName,
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        LocalDateTime ticketingTime,
        List<SeatGradeInfoResponse> seatGrades
) {

    public static ConcertDetailsInfoResponse from(
            ConcertEntity concert,
            List<SeatGradeInfoResponse> seatGradeInfoResponses) {
        return new ConcertDetailsInfoResponse(
                concert.getId(),
                concert.getConcertName(),
                concert.getPerformer(),
                concert.getShowStart(),
                concert.getShowEnd(),
                concert.getTicketingTime(),
                seatGradeInfoResponses);
    }
}
