package com.trove.ticket_trove.service.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketSeatCheckResponse;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import com.trove.ticket_trove.service.redis.TicketRedisService;
import com.trove.ticket_trove.service.reservation.util.ReservationUtilService;
import com.trove.ticket_trove.service.reservation.validation.ReservationValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationReadService {
    private final TicketRepository ticketRepository;
    private final TicketRedisService ticketRedisService;
    private final ReservationUtilService reservationUtilService;

    //유저 티켓 전체 조회 (유저)
    public List<TicketInfoResponse> searchTickets(
            MemberEntity memberEntity, Integer page, Integer size) { //시큐리티 적용시 수정할 예정

        Pageable pageable = PageRequest.of(page, size);

        return ticketRepository
                .findByMemberEmailOrderByCreatedAtAsc(memberEntity, pageable)
                .stream()
                .map(TicketInfoResponse::from)
                .toList();
    }

    //공연장 티켓 전체 조회(관리자)
    public List<TicketDetailResponse> searchConcertTickets(
            Long concertId, Integer page, Integer size) {

        String key = TicketRedisService.key(concertId);
        //ConcertId별 티켓 맵

        Map<Object, Object> cachedTicketMap = ticketRedisService.getTicketList(key);

        if(!cachedTicketMap.isEmpty()){
            return cachedTicketMap.values()
                    .stream()
                    .map(o -> (TicketDetailResponse) o).toList();
        }


        var concertEntity = reservationUtilService.getConcertEntity(concertId);

        var ticketEntityList = ticketRepository
                .findByConcertIdOrderByCreatedAtAsc(concertEntity);

        return ticketEntityList.stream()
                .map(ticketEntity -> {
                    ticketRedisService.save(ticketEntity);
                    return TicketDetailResponse.from(ticketEntity);}).toList();
    }

    //티켓 단건 조회
    public TicketInfoResponse searchTicket(
            MemberEntity memberEntity, TicketSearchRequest request) {

        var ticket = reservationUtilService.createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        var ticketDetailResponse = ticketRedisService.getTicketInfo(
                TicketRedisService.key(ticket.getConcertId().getId()),
                TicketRedisService.subKey(ticket));

        if (ticketDetailResponse != null) {
            return new TicketInfoResponse(
                    ticketDetailResponse.ticketId(),
                    ticketDetailResponse.name(),
                    ticketDetailResponse.concertName(),
                    ticketDetailResponse.performer(),
                    ticketDetailResponse.grade(),
                    ticketDetailResponse.seatNumber(),
                    ticketDetailResponse.showStart(),
                    ticketDetailResponse.showEnd());
        }

        var ticketEntity = reservationUtilService.getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        ticketRedisService.save(ticketEntity);

        return TicketInfoResponse.from(ticketEntity);
    }

    //좌석 체크
    public TicketSeatCheckResponse seatCheck(
            Long concertId, String grade, Integer seatNumber) {
        var concertEntity = reservationUtilService
                .getConcertEntity(concertId);
        var seatGradeEntity = reservationUtilService
                .getSeatGradeEntity(concertEntity, grade);
        //유효성 검사
        ReservationValidation.validateSeat(seatGradeEntity, seatNumber);
        //좌석이 있는지 티캣을 통해 확인

        return ticketRepository.findByConcertIdAndSeatGradeAndSeatNumber(
                        concertEntity, seatGradeEntity, seatNumber)
                .map(ticket -> new TicketSeatCheckResponse(false))
                .or(() -> Optional.of(new TicketSeatCheckResponse(true)))
                .get();
    }

}
