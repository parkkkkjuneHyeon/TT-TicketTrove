package com.trove.ticket_trove.service.concert;

import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.service.redis.ConcertRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertReadService {

    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRedisService concertRedisService;

    //콘서트 전체 조회
    public List<ConcertInfoResponse> searchConcerts(Integer page, Integer size) {
        Pageable pageable = null;
        if (!ObjectUtils.isEmpty(page) && !ObjectUtils.isEmpty(size))
            pageable = PageRequest.of(page, size);

//        List<ConcertInfoResponse> concertLists
//                = concertRedisService.getAllConcertInfo().stream().map(
//                detailInfo -> new ConcertInfoResponse(
//                            detailInfo.concertId(),
//                            detailInfo.concertName(),
//                            detailInfo.performer(),
//                            detailInfo.showStart(),
//                            detailInfo.showEnd(),
//                            detailInfo.ticketingTime())
//                ).toList();
//        if(!ObjectUtils.isEmpty(concertLists)){
//            return concertLists;
//        }

        return concertRepository.findAllByOrderByShowStartAsc(pageable).stream()
                .map(ConcertInfoResponse::from).toList();
    }

    //예매 임박 콘서트들 조회
    public List<ConcertInfoResponse> getImminentConcerts() {

        var something = concertRedisService.getConcertListForReservation();
        if(something == null) {
            makeRedisDataWhenStartApplication();
        }

        return concertRedisService.getConcertListForReservation()
                .stream()
                .map(
                    detailInfo -> new ConcertInfoResponse(
                            detailInfo.concertId(),
                            detailInfo.concertName(),
                            detailInfo.performer(),
                            detailInfo.showStart(),
                            detailInfo.showEnd(),
                            detailInfo.ticketingTime())
                )
                .toList();
    }

    //콘서트 단건 조회
    public ConcertDetailsInfoResponse searchConcert(Long concertId) {
        var concertInfo = concertRedisService.getConcertInfo(concertId);
        if(concertInfo != null){
            return concertInfo;
        }

        concertRedisService.deleteConcertInfo(concertId);

        var concertEntity = getConcertEntity(concertId);
        var seatGrades = seatGradeRepository
                .findByConcertIdOrderByPriceDesc(concertEntity)
                .stream().map(SeatGradeInfoResponse::from)
                .toList();

        return concertRedisService.save(
                ConcertDetailsInfoResponse.from(concertEntity, seatGrades)
        );
    }

    private ConcertEntity getConcertEntity(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(ConcertNotFoundException::new);
    }

    @Scheduled(fixedRate = 10800000)
    public void makeRedisDataWhenStartApplication(){
        var now = LocalDateTime.now();
        List<ConcertEntity> concertList = concertRepository
                .findConcertEntityOrderByTicketingTimeAsc(now);

        concertList.forEach(concertEntity -> {
            List<SeatGradeInfoResponse> seatGradeEntityList =
                    seatGradeRepository
                            .findByConcertId(concertEntity)
                            .stream()
                            .map(SeatGradeInfoResponse::from)
                            .toList();
            concertRedisService.saveList(
                    ConcertDetailsInfoResponse
                            .from(concertEntity, seatGradeEntityList)
            );
        });
    }

}
