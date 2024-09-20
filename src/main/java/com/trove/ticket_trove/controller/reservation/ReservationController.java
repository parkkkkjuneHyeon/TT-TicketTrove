package com.trove.ticket_trove.controller.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketSeatCheckResponse;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.service.reservation.ReservationReadService;
import com.trove.ticket_trove.service.reservation.ReservationWriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationController {
    private final ReservationReadService reservationReadService;
    private final ReservationWriteService reservationWriteService;

    public ReservationController(
            ReservationReadService reservationReadService,
            ReservationWriteService reservationWriteService) {
        this.reservationReadService = reservationReadService;
        this.reservationWriteService = reservationWriteService;
    }

    //티켓 예매
    @PostMapping
    public ResponseEntity<TicketReservationResponse> reserve(
            Authentication authentication,
            @RequestBody
            TicketCreateRequest request
    ) {
        var memberEntity = (MemberEntity) authentication.getPrincipal();
        var ticketResponse = reservationWriteService.reserve(
                memberEntity,
                request);
        return ResponseEntity.ok(ticketResponse);
    }
    //좌석 유무 확인
    @GetMapping("/check-seat/{concertId}")
    public ResponseEntity<TicketSeatCheckResponse> getSeatInfo(
            @PathVariable Long concertId,
            @RequestParam String grade,
            @RequestParam Integer seatNumber
    ){
        var seatCheckResponse = reservationReadService
                .seatCheck(concertId, grade.toUpperCase(), seatNumber);
        return ResponseEntity.ok(seatCheckResponse);
    }

    //유저 티켓 전체 조회
    @GetMapping("/member-tickets")
    public ResponseEntity<List<TicketInfoResponse>> searchMemberTickets(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size
    ) {
        var memberEntity = getMemberEntity(authentication);
        var memberTicketsResponse =
                reservationReadService.searchTickets(
                       memberEntity , page, size);
        return ResponseEntity.ok(memberTicketsResponse);
    }
    //유저 티켓 단건 조회
    @PostMapping("/member-ticket")
    public ResponseEntity<TicketInfoResponse> searchTicket(
            Authentication authentication,
            @RequestBody
            TicketSearchRequest request
    ) {
        var memberEntity = getMemberEntity(authentication);
        var ticketInfoResponse = reservationReadService.searchTicket(
                memberEntity,
                request);
        return ResponseEntity.ok(ticketInfoResponse);
    }

    //공연장 티켓 전체 조회
    @GetMapping("/concert-tickets/{concertId}")
    public ResponseEntity<List<TicketDetailResponse>> searchConcertTickets(
            @PathVariable Long concertId,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "3", required = false) Integer size
    ) {
        var concertTicketsResponse = reservationReadService
                .searchConcertTickets(concertId, page, size);
        return ResponseEntity.ok(concertTicketsResponse);
    }
    //티켓 취소
    @DeleteMapping
    public ResponseEntity<HttpStatus> cancelTicket(
            Authentication authentication,
            @RequestBody
            TicketDeleteRequest request
    ) {
        var memberEntity = getMemberEntity(authentication);
        reservationWriteService.cancelTicket(
                memberEntity,
                request);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private MemberEntity getMemberEntity(Authentication authentication) {
        return (MemberEntity) authentication.getPrincipal();
    }
}
