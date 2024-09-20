package com.trove.ticket_trove.service.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.dto.seatGrade.request.SeatGradeUpdateRequest;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeInfoResponse;
import com.trove.ticket_trove.dto.seatGrade.response.SeatGradeUpdateResponse;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.exception.seatgrade.SeatGradeNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import com.trove.ticket_trove.service.concert.validation.ConcertValidation;
import com.trove.ticket_trove.service.concert.validation.SeatGradeValidation;
import com.trove.ticket_trove.service.redis.ConcertRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertWriteService {
    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRedisService concertRedisService;

    //콘서트 생성
    @Transactional
    public void addConcert(ConcertCreateRequest request) {
        ConcertValidation.validateConcert(request.concertName(),
                request.performer(),
                request.showStart(),
                concertRepository
        );

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
        List<SeatGradeUpdateResponse> seatGrades;

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
        List<SeatGradeInfoResponse> seatGradeInfoResponses =
                seatGrades.stream()
                        .map(
                            seatGrade -> new SeatGradeInfoResponse(
                                    seatGrade.grade(),
                                    seatGrade.price(),
                                    seatGrade.totalSeat())
                        )
                        .toList();

        concertRedisService.updateConcertInfo(
                ConcertDetailsInfoResponse.from(concertEntity, seatGradeInfoResponses)
        );

        return ConcertUpdateResponse
                .from(concertRepository.save(concertEntity), seatGrades);
    }

    //등급 테이블 업데이트
    private List<SeatGradeUpdateResponse> updateSeatGrade(
            ConcertEntity concertEntity,
            List<SeatGradeUpdateRequest> seatGradeUpdateRequests) {

        return seatGradeUpdateRequests.stream()
                .map(sgq -> {

                    SeatGradeEntity seatGradeEntity;

                    if (!ObjectUtils.isEmpty(sgq.previousGrade())
                            && !ObjectUtils.isEmpty(sgq.previousPrice())
                            && ( !ObjectUtils.isEmpty(sgq.updateGrade())
                            || !ObjectUtils.isEmpty(sgq.updatePrice())
                            || !ObjectUtils.isEmpty(sgq.updateTotalSeat()) )) {

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

                        SeatGradeValidation.validateSeatGrade(
                                concertEntity,
                                sgq.updateGrade().toUpperCase(),
                                seatGradeRepository
                        );

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

                    }else
                        throw new SeatGradeNotFoundException();
                }).toList();
    }

    @Transactional
    //콘서트 정보 삭제
    public void deleteConcert(Long id) {
        var concertEntity = getConcertEntity(id);
        concertRedisService.deleteConcertInfo(id);
        seatGradeRepository.deleteAllByConcertId(concertEntity);
        concertRepository.delete(concertEntity);
    }

    private ConcertEntity getConcertEntity(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(ConcertNotFoundException::new);
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
}
