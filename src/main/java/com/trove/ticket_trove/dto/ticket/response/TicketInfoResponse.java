package com.trove.ticket_trove.dto.ticket.response;

import com.trove.ticket_trove.model.entity.ticket.TicketEntity;

import java.time.LocalDateTime;

public record TicketInfoResponse(
        Long id,
        String name,
        String concertName,
        String performer,
        String grade,
        Integer seatNumber,
        LocalDateTime showStart,
        LocalDateTime showEnd
) {
    public static TicketInfoResponse from(
            TicketEntity ticketEntity
    ) {
        var concertEntity = ticketEntity.getConcertId();
        return new TicketInfoResponse(
                ticketEntity.getId(),
                ticketEntity.getMemberEmail().getName(),
                concertEntity.getConcertName(),
                concertEntity.getPerformer(),
                ticketEntity.getSeatGrade().getGrade(),
                ticketEntity.getSeatNumber(),
                concertEntity.getShowStart(),
                concertEntity.getShowEnd());
    }


}
