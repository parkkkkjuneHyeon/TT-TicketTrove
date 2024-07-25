package com.trove.ticket_trove.dto.concert.response;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;

import java.time.LocalDateTime;

public record ConcertUpdateResponse(
        Long concertId,
        String concertName,
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd
) {

    public static ConcertUpdateResponse from(ConcertEntity concert) {
        return new ConcertUpdateResponse(
                concert.getId(),
                concert.getConcertName(),
                concert.getPerformer(),
                concert.getShowStart(),
                concert.getShowEnd());
    }
}
