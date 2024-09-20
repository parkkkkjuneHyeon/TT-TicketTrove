package com.trove.ticket_trove.service.reservation.validation;

import com.trove.ticket_trove.exception.seatgrade.SeatNumberValidationException;
import com.trove.ticket_trove.exception.ticket.TicketExistsException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import com.trove.ticket_trove.service.redis.TicketRedisService;

public class ReservationValidation {

    //등급의 좌석수보다 높으면 예외
    public static void validateSeat(
            SeatGradeEntity seatGradeEntity,
            Integer seatNumber) {

        if(seatNumber > seatGradeEntity.getTotalSeat())
            throw new SeatNumberValidationException();
        else if(seatNumber < 1)
            throw new SeatNumberValidationException();
    }

    //이미 존재하는 티켓인지 확인
    public static void validateTicket(
            ConcertEntity concertEntity,
            SeatGradeEntity seatGradeEntity,
            MemberEntity memberEntity,
            Integer seatNumber,
            TicketRedisService ticketRedisService) {

        if (ticketRedisService.getTicketInfo(
                TicketRedisService.key(concertEntity.getId()),
                TicketRedisService.subKey(
                        memberEntity.getEmail(),
                        seatGradeEntity.getGrade(),
                        seatNumber)) != null) {

            throw new TicketExistsException();
        }
    }

    //이미 존재하는 티켓인지 확인
    public static void validateTicket(
            ConcertEntity concertEntity,
            SeatGradeEntity seatGradeEntity,
            MemberEntity memberEntity,
            Integer seatNumber,
            TicketRepository ticketRepository) {

        if (ticketRepository.findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
                        concertEntity,
                        seatGradeEntity,
                        memberEntity,
                        seatNumber
                )
                .isPresent()) {

            throw new TicketExistsException();
        }
    }
}
