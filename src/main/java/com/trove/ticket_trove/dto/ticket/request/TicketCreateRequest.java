package com.trove.ticket_trove.dto.ticket.request;

import jakarta.validation.constraints.NotBlank;

public record TicketCreateRequest(
        @NotBlank
        Long concertId,
        @NotBlank
        String seatGrade,
        @NotBlank
        Integer seatNumber) {
}
