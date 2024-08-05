package com.trove.ticket_trove.dto.concert.response;

import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeUpdateResponse;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertUpdateResponse(
        Long concertId,
        String concertName,
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        LocalDateTime ticketingTime,
        List<SeatGradeUpdateResponse> gradeTypes
) {

    public static ConcertUpdateResponse from(
            ConcertEntity concertEntity,
            List<SeatGradeUpdateResponse> gradeTypes) {
        return new ConcertUpdateResponse(
                concertEntity.getId(),
                concertEntity.getConcertName(),
                concertEntity.getPerformer(),
                concertEntity.getShowStart(),
                concertEntity.getShowEnd(),
                concertEntity.getTicketingTime(),
                gradeTypes);
    }
}
