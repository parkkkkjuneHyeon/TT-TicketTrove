package com.trove.ticket_trove.service.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketSeatCheckResponse;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatNumberValidationException;
import com.trove.ticket_trove.exception.ticket.TicketExistsException;
import com.trove.ticket_trove.exception.ticket.TicketNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import com.trove.ticket_trove.service.concert.ConcertService;
import com.trove.ticket_trove.service.redis.ConcertRedisService;
import com.trove.ticket_trove.service.redis.TicketRedisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final TicketRepository ticketRepository;
    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final TicketRedisService ticketRedisService;
    private final ConcertService concertService;

    //티켓 예매
    @Transactional
    public TicketReservationResponse reserve(
            MemberEntity memberEntity, TicketCreateRequest request
    ) {

        var ticket = createTicketEntity(
                request.concertId(), memberEntity,
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


        var ticketEntity = ticketRepository.save(ticket);
        ticketRedisService.save(ticketEntity);
        return TicketReservationResponse.from(ticketEntity);
    }

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


        var concertEntity = getConcertEntity(concertId);

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

        var ticket = createTicketEntity(
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

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        ticketRedisService.save(ticketEntity);

        return TicketInfoResponse.from(ticketEntity);
    }

    //티켓 예매 취소
    @Transactional
    public void cancelTicket(
            MemberEntity memberEntity,
            TicketDeleteRequest request) {

        var ticket = createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        String key = TicketRedisService.key(ticketEntity.getConcertId().getId());
        String subKey = TicketRedisService.subKey(ticketEntity);

        ticketRedisService.delete(key, subKey);
        ticketRepository.delete(ticketEntity);

    }


    //좌석 체크
    public TicketSeatCheckResponse seatCheck(
            Long concertId, String grade, Integer seatNumber) {
        var concertEntity = getConcertEntity(concertId);
        var seatGradeEntity = getSeatGradeEntity(concertEntity, grade);
        //유효성 검사
        validateSeat(seatGradeEntity, seatNumber);
        //좌석이 있는지 티캣을 통해 확인


        return ticketRepository.findByConcertIdAndSeatGradeAndSeatNumber(
                        concertEntity, seatGradeEntity, seatNumber)
                .map(ticket -> new TicketSeatCheckResponse(false))
                .or(() -> Optional.of(new TicketSeatCheckResponse(true)))
                .get();
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


        if (ticketRedisService.getTicketInfo(
                TicketRedisService.key(concertEntity.getId()),
                TicketRedisService.subKey(
                        memberEntity.getEmail(),
                        seatGradeEntity.getGrade(),
                        seatNumber)) != null) {

            throw new TicketExistsException();
        }
   }

    //Ticket 객체를 생성하는 메소드
    private TicketEntity createTicketEntity(
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
    private TicketEntity getTicketEntity(
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

    private ConcertEntity getConcertEntity(Long concertId) {

         return ConcertEntity.from(concertService.searchConcert(concertId));
    }

    //TODO
    private SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity,
            String grade) {

        //키를 인식을 못해서 못찾는건지 계속 디비로 접근함.
        return seatGradeRepository
                    .findByConcertIdAndGrade(
                            concertEntity, grade.toUpperCase())
                    .orElseThrow(SeatGradeNotFoundException::new);
    }
}
