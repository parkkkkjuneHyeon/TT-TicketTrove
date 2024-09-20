package com.trove.ticket_trove.controller.concert;

import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
import com.trove.ticket_trove.dto.concert.response.ConcertUpdateResponse;
import com.trove.ticket_trove.service.concert.ConcertReadService;
import com.trove.ticket_trove.service.concert.ConcertWriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/concert")
public class ConcertController {
    private final ConcertReadService concertReadService;
    private final ConcertWriteService concertWriteService;

    public ConcertController(
            ConcertReadService concertReadService,
            ConcertWriteService concertWriteService) {
        this.concertReadService = concertReadService;
        this.concertWriteService = concertWriteService;
    }

    //콘서트 생성
    @PostMapping
    public ResponseEntity<HttpStatus> addConcert(
            @RequestBody
            ConcertCreateRequest request
    ) {
        concertWriteService.addConcert(request);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    //콘서트 조회
    @GetMapping
    public ResponseEntity<List<ConcertInfoResponse>> getConcerts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        var concertResponses = concertReadService.searchConcerts(page, size);
        return ResponseEntity.ok(concertResponses);
    }

    // 첫 화면 예매임박 콘서트리스트
    @GetMapping("/imminent")
    public ResponseEntity<List<ConcertInfoResponse>> imminentConcert(
    ) {
        var concertResponses = concertReadService.getImminentConcerts();
        return ResponseEntity.ok(concertResponses);
    }

    //콘서트 단건 조회
    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertDetailsInfoResponse> getConcert(
            @PathVariable Long concertId) {
        var concertDetailsResponse = concertReadService.searchConcert(concertId);
        return ResponseEntity.ok(concertDetailsResponse);
    }

    //콘서트정보 수정
    @PatchMapping
    public ResponseEntity<ConcertUpdateResponse> updateConcert(
            @RequestBody
            ConcertUpdateRequest request
    ){
        var concertUpdateResp = concertWriteService.updateConcert(request);

        return ResponseEntity.ok(concertUpdateResp);
    }

    //콘서트 삭제
    @DeleteMapping("/{concertId}")
    public ResponseEntity<HttpStatus> deleteConcert(
            @PathVariable Long concertId
    ) {
        concertWriteService.deleteConcert(concertId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
