package com.trove.ticket_trove.service.reservation;

import com.trove.ticket_trove.dto.ticket.request.TicketCreateRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketDeleteRequest;
import com.trove.ticket_trove.dto.ticket.request.TicketSearchRequest;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoAdminResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketInfoResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketReservationResponse;
import com.trove.ticket_trove.dto.ticket.response.TicketSeatCheckResponse;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatNumberValidationException;
import com.trove.ticket_trove.exception.ticket.TicketExistsException;
import com.trove.ticket_trove.exception.ticket.TicketNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.concert.RedisHashConcert;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.entity.seat_grade.RedisHashSeatGrade;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.entity.ticket.RedisHashTicket;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.concert.RedisHashConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.RedisHashSeatGradeRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.model.storage.ticket.RedisHashTicketRepository;
import com.trove.ticket_trove.model.storage.ticket.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisHashTicketRepository redisHashTicketRepository;
    private final RedisHashConcertRepository redisHashConcertRepository;
    private final RedisTemplate<String, TicketEntity> ticketRedisTemplate;
    private final RedisTemplate redisTemplate;
    private final RedisHashSeatGradeRepository redisHashSeatGradeRepository;

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

        // Ticket:key -> 티켓 키 값을 따라 ticket저장
        redisHashTicketRepository.save(RedisHashTicket.from(ticket));
        // Ticket:concertId -> 콘서트장 좌석조회용도, 콘서트 아이디값을 따라 ticket저장
        ticketRedisTemplate.opsForHash().put(
                "ConcertId:%d".formatted(ticket.getConcertId().getId()),
                RedisHashTicket.from(ticket).getKey(), ticket);
        return TicketReservationResponse
                .from(ticketRepository.save(ticket));
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
    public List<TicketInfoAdminResponse> searchConcertTickets(
            Long concertId, Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        var concertEntity = getConcertEntity(concertId);

        String key = "ConcertId:" + concertId;
        //ConcertId별 티켓 맵
        Map<Object, Object> cachedTicketMap = ticketRedisTemplate.opsForHash().entries(key);

        if(!cachedTicketMap.isEmpty()){
            return cachedTicketMap.values().stream().map(TicketInfoAdminResponse::from).toList();
        }

        var ticketEntityList = ticketRepository.findByConcertIdOrderByCreatedAtAsc(concertEntity);

        return ticketEntityList.stream().map(ticketEntity -> {
                    ticketRedisTemplate.opsForHash().put(key, RedisHashTicket.from(ticketEntity).getKey(), ticketEntity);
                    return TicketInfoAdminResponse.from(ticketEntity);})
                .toList();
    }

    //티켓 단건 조회
    public TicketInfoResponse searchTicket(MemberEntity memberEntity, TicketSearchRequest request) {

        var ticket = createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        return TicketInfoResponse.from(ticketEntity);
    }

    //티켓 예매 취소
    @Transactional
    public void cancelTicket(MemberEntity memberEntity, TicketDeleteRequest request) {

        var ticket = createTicketEntity(
                request.concertId(), memberEntity,
                request.seatGrade(), request.seatNumber());

        var ticketEntity = getTicketEntity(
                ticket.getConcertId(), ticket.getSeatGrade(),
                ticket.getMemberEmail(), ticket.getSeatNumber());

        String key = "ConcertId:" + ticket.getConcertId().getId();

        redisHashTicketRepository.delete(RedisHashTicket.from(ticket));
        redisTemplate.opsForHash().delete(key, RedisHashTicket.from(ticket).getKey());
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

        // redis에서 TicketExistsException() 처리하기 VS Redis & DB 확인후 TicketExistsException() 처리하기
        String key = RedisHashTicket.ofKey(
                memberEntity.getEmail(),
                concertEntity.getId(),
                seatGradeEntity.getGrade(),
                seatNumber);

        //redis에서 확인하기
        redisHashTicketRepository
                .findById(key)
                .map(TicketEntity::to)
                .ifPresent(ticket -> {throw new TicketExistsException();});

        //redis확인 후 db 확인하기
//        ticketRepository.findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
//                        concertEntity, seatGradeEntity, memberEntity, seatNumber)
//                .ifPresent(ticket -> {throw new TicketExistsException();});
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

        String key = RedisHashTicket.ofKey(
                memberEntity.getEmail(),
                concertEntity.getId(),
                seatGradeEntity.getGrade(),
                seatNumber);

        var cachedTicket = redisHashTicketRepository.findById(key).orElseGet(()->{
            var ticketEntity = ticketRepository.findByConcertIdAndSeatGradeAndMemberEmailAndSeatNumber(
                    concertEntity, seatGradeEntity, memberEntity, seatNumber).orElseThrow(TicketNotFoundException::new);
            return redisHashTicketRepository.save(RedisHashTicket.from(ticketEntity));
        });

        return TicketEntity.to(cachedTicket);
    }

    private ConcertEntity getConcertEntity(Long concertId) {

        var cachedConcert = redisHashConcertRepository.findById("Concert:"+concertId).orElseGet(() ->{
            var concertEntity = concertRepository.findById(concertId).orElseThrow(ConcertNotFoundException::new);
            return redisHashConcertRepository.save(RedisHashConcert.from(concertEntity));
                }
        );
        return ConcertEntity.to(cachedConcert);
    }

    private SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity,
            String grade) {
        String key = RedisHashSeatGrade.ofKey(concertEntity.getId(), grade);

        var cachedConcert = redisHashSeatGradeRepository.findById("SeatGrade:"+key).orElseGet(() ->{
                    var seatGradeEntity = seatGradeRepository.findByConcertIdAndGrade(
                        concertEntity, grade.toUpperCase())
                            .orElseThrow(SeatGradeNotFoundException::new);
                    return redisHashSeatGradeRepository.save(RedisHashSeatGrade.from(seatGradeEntity));
                }
        );
        return SeatGradeEntity.to(cachedConcert);
    }
}
