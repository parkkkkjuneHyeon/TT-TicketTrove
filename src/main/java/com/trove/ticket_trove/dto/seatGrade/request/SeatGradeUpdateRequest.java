package com.trove.ticket_trove.dto.seatGrade.request;

import jakarta.validation.constraints.NotBlank;

public record SeatGradeUpdateRequest(
        @NotBlank
        String previousGrade,
        @NotBlank
        Integer previousPrice,
        String updateGrade,
        Integer updatePrice,
        Integer updateTotalSeat) {}
