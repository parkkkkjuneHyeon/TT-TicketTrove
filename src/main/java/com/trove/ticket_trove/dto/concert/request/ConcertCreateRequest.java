package com.trove.ticket_trove.dto.concert.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ConcertCreateRequest(
        @NotBlank
        String concertName,
        @NotBlank
        String performer,
        @NotBlank
        LocalDateTime showStart,
        @NotBlank
        LocalDateTime showEnd) {
}
