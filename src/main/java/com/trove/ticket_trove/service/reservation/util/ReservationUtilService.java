package com.trove.ticket_trove.service.reservation.util;

import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.exception.ticket.TicketNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import com.trove.ticket_trove.service.concert.ConcertReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationUtilService {
    private final TicketRepository ticketRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ConcertReadService concertReadService;

    //Ticket 객체를 생성하는 메소드
    public TicketEntity createTicketEntity(
            Long concertId, MemberEntity memberEntity,
            String grade, Integer seatNumber) {

        var concertEntity = getConcertEntity(concertId);
        var seatGradeEntity =
                getSeatGradeEntity(concertEntity, grade.toUpperCase());

        return TicketEntity.from(
                memberEntity, concertEntity,
                seatGradeEntity, seatNumber);
    }

    //DB에 저장된 Ticket 조회해서 가져오는 메소드
    public TicketEntity getTicketEntity(
            ConcertEntity concertEntity,
            SeatGradeEntity seatGradeEntity,
            MemberEntity memberEntity,
            Integer seatNumber) {

        return ticketRepository
                .findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
                        concertEntity, seatGradeEntity,
                        memberEntity, seatNumber)
                .orElseThrow(TicketNotFoundException::new);
    }

    public ConcertEntity getConcertEntity(Long concertId) {

        return ConcertEntity.from(concertReadService.searchConcert(concertId));
    }

    //TODO
    public SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity,
            String grade) {

        //키를 인식을 못해서 못찾는건지 계속 디비로 접근함.
        return seatGradeRepository
                .findByConcertIdAndGrade(
                        concertEntity, grade.toUpperCase())
                .orElseThrow(SeatGradeNotFoundException::new);
    }
}
