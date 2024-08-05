package com.trove.ticket_trove.dto.concert.request;

import com.trove.ticket_trove.dto.seatGrade.request.SeatGradeUpdateRequest;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertUpdateRequest(
        @NotBlank
        Long concertId,
        String concertName,
        String performer,
        LocalDateTime showStart,
        LocalDateTime showEnd,
        LocalDateTime ticketingTime,
        List<SeatGradeUpdateRequest> gradeTypes) {
}
