package com.trove.ticket_trove.service.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import com.trove.ticket_trove.service.redis.TicketRedisService;
import com.trove.ticket_trove.service.reservation.util.ReservationUtilService;
import com.trove.ticket_trove.service.reservation.validation.ReservationValidation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationWriteService {
    private final TicketRepository ticketRepository;
    private final ReservationUtilService reservationUtilService;
    private final TicketRedisService ticketRedisService;


    //티켓 예매
    @Transactional
    public TicketReservationResponse reserve(
            MemberEntity memberEntity, TicketCreateRequest request
    ) {

        var ticket = reservationUtilService.createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        //유효성 검사
        ReservationValidation.validateSeat(
                ticket.getSeatGrade(),
                ticket.getSeatNumber());
        ReservationValidation.validateTicket(
                ticket.getConcertId(),
                ticket.getSeatGrade(),
                ticket.getMemberEmail(),
                ticket.getSeatNumber(),
                ticketRedisService);

        var ticketEntity = ticketRepository.save(ticket);
        ticketRedisService.save(ticketEntity);

        return TicketReservationResponse.from(ticketEntity);
    }

    //티켓 예매 취소
    @Transactional
    public void cancelTicket(
            MemberEntity memberEntity,
            TicketDeleteRequest request) {

        var ticket = reservationUtilService.createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        var ticketEntity = reservationUtilService.getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        String key = TicketRedisService.key(ticketEntity.getConcertId().getId());
        String subKey = TicketRedisService.subKey(ticketEntity);

        ticketRedisService.delete(key, subKey);
        ticketRepository.delete(ticketEntity);
    }
}
