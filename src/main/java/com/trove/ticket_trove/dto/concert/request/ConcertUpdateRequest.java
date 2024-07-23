package com.trove.ticket_trove.dto.concert.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ConcertUpdateRequest(
        @NotBlank
        Long concertId,
        @NotBlank
        String concertName,
        @NotBlank
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd) {
}
