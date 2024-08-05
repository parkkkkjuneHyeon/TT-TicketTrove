package com.trove.ticket_trove.dto.seatGrade.request;

import jakarta.validation.constraints.NotBlank;

public record SeatGradeCreateRequest(
        @NotBlank
        String grade,
        @NotBlank
        Integer price,
        @NotBlank
        Integer totalSeat) {}
