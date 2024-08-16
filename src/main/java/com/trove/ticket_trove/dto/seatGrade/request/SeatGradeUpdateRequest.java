package com.trove.ticket_trove.dto.seatGrade.request;

public record SeatGradeUpdateRequest(
        String previousGrade,
        Integer previousPrice,
        String updateGrade,
        Integer updatePrice,
        Integer updateTotalSeat) {}
