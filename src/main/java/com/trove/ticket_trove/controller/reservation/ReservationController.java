package com.trove.ticket_trove.controller.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketMemberEmailRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoAdminResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.service.reservation.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    //티켓 예매
    @PostMapping
    public ResponseEntity<TicketReservationResponse> reserve(
            @RequestBody
            TicketCreateRequest request
    ) {
        var ticketResponse = reservationService.reserve(request);
        return ResponseEntity.ok(ticketResponse);
    }
    //유저 티켓 전체 조회
    @PostMapping("/member-tickets")
    public ResponseEntity<List<TicketInfoResponse>> searchMemberTickets(
            @RequestBody
            TicketMemberEmailRequest request,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size
    ) {
        var memberTicketsResponse =
                reservationService.searchTickets(request.email(), page, size);
        return ResponseEntity.ok(memberTicketsResponse);
    }
    //유저 티켓 단건 조회
    @PostMapping("/member-ticket")
    public ResponseEntity<TicketInfoResponse> searchTicket(
            @RequestBody
            TicketSearchRequest request
    ) {
        var ticketInfoResponse = reservationService.searchTicket(request);
        return ResponseEntity.ok(ticketInfoResponse);
    }
    //공연장 티켓 전체 조회
    @GetMapping("/concert-tickets/{concertId}")
    public ResponseEntity<List<TicketInfoAdminResponse>> searchConcertTickets(
            @PathVariable Long concertId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size
    ) {
        var concertTicketsResponse = reservationService
                .searchConcertTickets(concertId, page, size);
        return ResponseEntity.ok(concertTicketsResponse);
    }
    //티켓 취소
    @DeleteMapping
    public ResponseEntity<HttpStatus> cancelTicket(
            @RequestBody
            TicketDeleteRequest request
    ) {
        reservationService.cancelTicket(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
