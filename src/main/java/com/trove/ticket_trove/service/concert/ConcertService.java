package com.trove.ticket_trove.service.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.dto.seatGrade.request.SeatGradeUpdateRequest;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeUpdateResponse;
import com.trove.ticket_trove.exception.concert.ConcertExistsException;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeExistsException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.service.redis.ConcertRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRedisService concertRedisService;

    //콘서트 생성
    @Transactional
    public void addConcert(ConcertCreateRequest request) {
        validateConcert(request.concertName(),
                request.performer(), request.showStart());

        var concert = ConcertEntity.builder()
                .concertName(request.concertName())
                .performer(request.performer())
                .showStart(request.showStart())
                .showEnd(request.showEnd())
                .ticketingTime(request.ticketingTime())
                .build();

        var seatGradeCreateRequests = request.gradeTypes();
        //공연장 저장
        var concertEntity = concertRepository.save(concert);
        //공연장 등급 저장
        seatGradeCreateRequests.stream()
                .map(seatGradeCreateRequest -> {
                    var seatGradeEntity = SeatGradeEntity.from(
                            concertEntity,
                            seatGradeCreateRequest.grade().toUpperCase(),
                            seatGradeCreateRequest.price(),
                            seatGradeCreateRequest.totalSeat());
                    seatGradeRepository.save(seatGradeEntity);

                    return SeatGradeInfoResponse.from(seatGradeEntity);
                }).toList();
    }

    @Transactional
    //콘서트 정보 수정
    public ConcertUpdateResponse updateConcert(ConcertUpdateRequest request) {
        var concertEntity = getConcertEntity(request.concertId());
        List<SeatGradeUpdateResponse> seatGrades = null;

        if (!ObjectUtils.isEmpty(request.concertName()))
            concertEntity.setConcertName(request.concertName());

        if (!ObjectUtils.isEmpty(request.performer()))
            concertEntity.setPerformer(request.performer());

        if (!ObjectUtils.isEmpty(request.showStart()))
            concertEntity.setShowStart(request.showStart());

        if (!ObjectUtils.isEmpty(request.showEnd()))
            concertEntity.setShowEnd(request.showEnd());

        if (!ObjectUtils.isEmpty(request.ticketingTime()))
            concertEntity.setTicketingTime(request.ticketingTime());

        if (!ObjectUtils.isEmpty(request.gradeTypes())) {
            seatGrades = updateSeatGrade(concertEntity, request.gradeTypes());
        } else {
            seatGrades = seatGradeRepository.findByConcertIdOrderByPriceDesc(concertEntity)
                    .stream()
                    .map(SeatGradeUpdateResponse::from)
                    .toList();
        }
        List<SeatGradeInfoResponse> seatGradeInfoResponses
                = seatGrades.stream().map(
                        seatGrade -> new SeatGradeInfoResponse(
                                    seatGrade.grade(),
                                    seatGrade.price(),
                                    seatGrade.totalSeat())
                        ).toList();

        concertRedisService.updateConcertInfo(
                ConcertDetailsInfoResponse.from(concertEntity,seatGradeInfoResponses)
        );

        return ConcertUpdateResponse
                .from(concertRepository.save(concertEntity), seatGrades);
    }

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

        return concertRedisService.getConcertListForReservation().stream().map(
                detailInfo -> new ConcertInfoResponse(
                        detailInfo.concertId(),
                        detailInfo.concertName(),
                        detailInfo.performer(),
                        detailInfo.showStart(),
                        detailInfo.showEnd(),
                        detailInfo.ticketingTime())
        ).toList();
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

        return concertRedisService.save(ConcertDetailsInfoResponse.from(concertEntity, seatGrades));
    }

    @Transactional
    //콘서트 정보 삭제
    public void deleteConcert(Long id) {
        var concertEntity = getConcertEntity(id);
        concertRedisService.deleteConcertInfo(id);
        seatGradeRepository.deleteAllByConcertId(concertEntity);
        concertRepository.delete(concertEntity);
    }

    //등급 테이블 업데이트
    private List<SeatGradeUpdateResponse> updateSeatGrade(
            ConcertEntity concertEntity,
            List<SeatGradeUpdateRequest> seatGradeUpdateRequests) {

        return seatGradeUpdateRequests.stream()
                .map(sgq -> {
                    SeatGradeEntity seatGradeEntity =  null;
                    if (!ObjectUtils.isEmpty(sgq.previousGrade())
                        && !ObjectUtils.isEmpty(sgq.previousPrice())
                        && ( !ObjectUtils.isEmpty(sgq.updateGrade())
                            || !ObjectUtils.isEmpty(sgq.updatePrice())
                            || !ObjectUtils.isEmpty(sgq.updateTotalSeat())) ) {
                        seatGradeEntity = getSeatGradeEntity(
                                concertEntity,
                                sgq.previousGrade(),
                                sgq.previousPrice());
                        if (!ObjectUtils.isEmpty(sgq.updateGrade()))
                            seatGradeEntity.setGrade(sgq.updateGrade());

                        if (!ObjectUtils.isEmpty(sgq.updatePrice()))
                            seatGradeEntity.setPrice(sgq.updatePrice());

                        if (!ObjectUtils.isEmpty(sgq.updateTotalSeat()))
                            seatGradeEntity.setTotalSeat(sgq.updateTotalSeat());

                        return SeatGradeUpdateResponse.from(
                                seatGradeRepository.save(seatGradeEntity));
                    }else if (ObjectUtils.isEmpty(sgq.previousGrade())
                            && ObjectUtils.isEmpty(sgq.previousPrice())
                            && !ObjectUtils.isEmpty(sgq.updateGrade())
                            && !ObjectUtils.isEmpty(sgq.updatePrice())
                            && !ObjectUtils.isEmpty(sgq.updateTotalSeat())) {
                        validateSeatGrade(concertEntity, sgq.updateGrade().toUpperCase());
                        seatGradeEntity = SeatGradeEntity.from(
                                concertEntity,
                                sgq.updateGrade(),
                                sgq.updatePrice(),
                                sgq.updateTotalSeat());
                        return SeatGradeUpdateResponse
                                .from(seatGradeRepository.save(seatGradeEntity));
                    }else if (!ObjectUtils.isEmpty(sgq.previousGrade())
                            && !ObjectUtils.isEmpty(sgq.previousPrice())
                            && ObjectUtils.isEmpty(sgq.updateGrade())
                            && ObjectUtils.isEmpty(sgq.updatePrice())
                            && ObjectUtils.isEmpty(sgq.updateTotalSeat())) {
                        seatGradeEntity = SeatGradeEntity.from(
                                concertEntity,
                                null,
                                null,
                                null);
                        seatGradeRepository
                                .deleteByConcertIdAndGradeAndPrice(
                                        concertEntity, sgq.previousGrade(), sgq.previousPrice());
                        return SeatGradeUpdateResponse.from(seatGradeEntity);
                    }else {
                        throw new SeatGradeNotFoundException();
                    }
                }).toList();
    }

    private SeatGradeEntity getSeatGradeEntity(
            ConcertEntity concertEntity, String grade, Integer price) {
        return seatGradeRepository
                .findByConcertIdAndGradeAndPrice(
                        concertEntity,
                        grade.toUpperCase(),
                        price)
                .orElseThrow(SeatGradeNotFoundException::new);
    }
    private void validateSeatGrade(
            ConcertEntity concertEntity, String grade) {
        seatGradeRepository
                .findByConcertIdAndGrade(
                        concertEntity,
                        grade.toUpperCase())
                .ifPresent(sg -> {throw new SeatGradeExistsException();});
    }
    private ConcertEntity
    getConcertEntity(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(ConcertNotFoundException::new);
    }

    private void validateConcert(
            String concertName,
            String performer,
            LocalDateTime showStart) {
        concertRepository.findByConcertNameAndPerformerAndShowStart(
                concertName, performer, showStart)
                .ifPresent(concert -> {
                    throw new ConcertExistsException(concertName, performer, showStart);
        });
    }

    @Scheduled(fixedRate = 10800000)
    public void makeRedisDataWhenStartApplication(){
        var now = LocalDateTime.now();
        List<ConcertEntity> concertList = concertRepository.findConcertEntityOrderByTicketingTimeAsc(now);

        concertList.forEach(concertEntity -> {
            List<SeatGradeInfoResponse> seatGradeEntityList =
                    seatGradeRepository
                            .findByConcertId(concertEntity).stream().map(SeatGradeInfoResponse::from).toList();
            concertRedisService.saveList(ConcertDetailsInfoResponse.from(concertEntity, seatGradeEntityList));
        });
    }

}
