package com.trove.ticket_trove.service.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.exception.concert.ConcertExistsException;
import com.trove.ticket_trove.exception.concert.ConcertNotFoundException;
import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
import com.trove.ticket_trove.model.storage.seat_grade.SeatGradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final SeatGradeRepository seatGradeRepository;

    public ConcertService(
            ConcertRepository concertRepository,
            SeatGradeRepository seatGradeRepository) {

        this.concertRepository = concertRepository;
        this.seatGradeRepository = seatGradeRepository;
    }

    @Transactional
    //콘서트 생성
    public void addConcert(ConcertCreateRequest request) {
        validateConcert(request.concertName(), request.performer());
        var concertEntity = ConcertEntity.builder()
                .concertName(request.concertName())
                .performer(request.performer())
                .showStart(request.showStart())
                .showEnd(request.showEnd())
                .build();
        var gradeType = request.gradeType();

        concertRepository.save(concertEntity);

        //공연장 등급 저장
        Arrays.stream(gradeType).map(sg ->
                SeatGradeEntity.from(
                        concertEntity, sg.grade().toUpperCase(),
                        sg.price(), sg.totalSeat())
        ).forEach(seatGradeRepository::save);
    }

    @Transactional
    //콘서트 정보 수정
    public ConcertUpdateResponse updateConcert(ConcertUpdateRequest request) {
        var concertEntity = getConcertEntity(request.concertId());

        if (!ObjectUtils.isEmpty(request.concertName()))
            concertEntity.setConcertName(request.concertName());

        if (!ObjectUtils.isEmpty(request.performer()))
            concertEntity.setPerformer(request.performer());

        if (!ObjectUtils.isEmpty(request.showStart()))
            concertEntity.setShowStart(request.showStart());

        if (!ObjectUtils.isEmpty(request.showEnd()))
            concertEntity.setShowEnd(request.showEnd());
        return ConcertUpdateResponse
                .from(concertRepository.save(concertEntity));
    }

    //콘서트 전체 조회
    public List<ConcertInfoResponse> searchConcerts() {
        return concertRepository.findAll().stream()
                .map(ConcertInfoResponse::from).toList();
    }

    //콘서트 단건 조회
    public ConcertInfoResponse searchConcert(Long id) {

        return ConcertInfoResponse.from(getConcertEntity(id));
    }

    @Transactional
    //콘서트 정보 삭제
    public void deleteConcert(Long id) {
        var concertEntity = getConcertEntity(id);
        seatGradeRepository.deleteAllByConcertId(concertEntity);
        concertRepository.delete(concertEntity);
    }

    private ConcertEntity getConcertEntity(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(ConcertNotFoundException::new);
    }

    private void validateConcert(String concertName, String performer) {
        concertRepository.findByConcertNameAndPerformer(
                concertName, performer)
                .ifPresent(concert -> {
                    throw new ConcertExistsException(concertName, performer);
        });
    }
}
