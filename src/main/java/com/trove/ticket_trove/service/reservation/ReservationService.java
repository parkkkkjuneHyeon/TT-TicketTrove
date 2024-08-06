package com.trove.ticket_trove.service.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoAdminResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatNumberValidationException;
import com.trove.ticket_trove.exception.ticket.TicketExistsException;
import com.trove.ticket_trove.exception.ticket.TicketNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    private final TicketRepository ticketRepository;
    private final ConcertRepository concertRepository;
    private final MemberRepository memberRepository;
    private final SeatGradeRepository seatGradeRepository;

    public ReservationService(
            TicketRepository ticketRepository,
            ConcertRepository concertRepository,
            MemberRepository memberRepository,
            SeatGradeRepository seatGradeRepository) {

        this.ticketRepository = ticketRepository;
        this.concertRepository = concertRepository;
        this.memberRepository = memberRepository;
        this.seatGradeRepository = seatGradeRepository;
    }

    //티켓 예매
    @Transactional
    public TicketReservationResponse reserve(
            TicketCreateRequest request
    ) {

        var ticket = createTicketEntity(
                request.concertId(), request.email(),
                request.seatGrade(), request.seatNumber());

        //유효성 검사
        validateSeat(
                ticket.getSeatGrade(),
                ticket.getSeatNumber());
        validateTicket(
                ticket.getConcertId(),
                ticket.getSeatGrade(),
                ticket.getMemberEmail(),
                ticket.getSeatNumber());

        return TicketReservationResponse
                .from(ticketRepository.save(ticket));
    }

    //유저 티켓 전체 조회 (유저)
    public List<TicketInfoResponse> searchTickets(
            String email, Integer page, Integer size) { //시큐리티 적용시 수정할 예정

        var memberEntity = getMemberEntity(email);
        Pageable pageable = PageRequest.of(page, size);

        return ticketRepository
                .findByMemberEmailOrderByCreatedAtAsc(memberEntity, pageable)
                .stream()
                .map(TicketInfoResponse::from)
                .toList();
    }

    //공연장 티켓 전체 조회(관리자)
    public List<TicketInfoAdminResponse> searchConcertTickets(
            Long concertId, Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        var concertEntity = getConcertEntity(concertId);

        return ticketRepository
                .findByConcertIdOrderByCreatedAtAsc(concertEntity, pageable)
                .stream().map(TicketInfoAdminResponse::from)
                .toList();
    }

    //티켓 단건 조회
    public TicketInfoResponse searchTicket(TicketSearchRequest request) {

        var ticket = createTicketEntity(
                request.concertId(), request.email(),
                request.seatGrade(), request.seatNumber());

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        return TicketInfoResponse.from(ticketEntity);
    }

    //티켓 예매 취소
    @Transactional
    public void cancelTicket(TicketDeleteRequest request) {

        var ticket = createTicketEntity(
                request.concertId(), request.email(),
                request.seatGrade(), request.seatNumber());

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        ticketRepository.delete(ticketEntity);
    }

    //등급의 좌석수보다 높으면 예외
    private void validateSeat(
            SeatGradeEntity seatGradeEntity,
            Integer seatNumber) {

        if(seatNumber > seatGradeEntity.getTotalSeat())
            throw new SeatNumberValidationException();
        else if(seatNumber < 1)
            throw new SeatNumberValidationException();
    }

    //이미 존재하는 티켓인지 확인
    private void validateTicket(
            ConcertEntity concertEntity,
            SeatGradeEntity seatGradeEntity,
            MemberEntity memberEntity,
            Integer seatNumber) {

        ticketRepository.findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
                        concertEntity, seatGradeEntity, memberEntity, seatNumber)
                .ifPresent(ticket -> {throw new TicketExistsException();});
    }

    //Ticket 객체를 생성하는 메소드
    private TicketEntity createTicketEntity(
            Long concertId, String email,
            String grade, Integer seatNumber) {

        var concertEntity = getConcertEntity(concertId);
        var seatGradeEntity =
                getSeatGradeEntity(concertEntity, grade.toUpperCase());
        var memberEntity = getMemberEntity(email);

        return TicketEntity.from(
                memberEntity, concertEntity,
                seatGradeEntity, seatNumber);
    }

    //DB에 저장된 Ticket 조회해서 가져오는 메소드
    private TicketEntity getTicketEntity(
            ConcertEntity concertEntity,
            SeatGradeEntity seatGradeEntity,
            MemberEntity memberEntity,
            Integer seatNumber) {

        return ticketRepository.findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
                concertEntity, seatGradeEntity, memberEntity, seatNumber)
                .orElseThrow(TicketNotFoundException::new);
    }

    private ConcertEntity getConcertEntity(Long concertId) {
        return concertRepository
                .findById(concertId)
                .orElseThrow(ConcertNotFoundException::new);
    }

    private SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity,
            String grade) {
        return seatGradeRepository
                .findByConcertIdAndGrade(
                        concertEntity, grade.toUpperCase())
                .orElseThrow(SeatGradeNotFoundException::new);
    }

    private MemberEntity getMemberEntity(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}
