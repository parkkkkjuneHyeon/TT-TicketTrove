package com.trove.ticket_trove.dto.concert.request;

import com.trove.ticket_trove.dto.seatGrade.request.SeatGradeCreateRequest;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertCreateRequest(
        @NotBlank
        String concertName,
        @NotBlank
        String performer,
        @NotBlank
        LocalDateTime showStart,
        @NotBlank
        LocalDateTime showEnd,
        @NotBlank
        LocalDateTime ticketingTime,
        @NotBlank
        List<SeatGradeCreateRequest> gradeTypes
        ) {

}

