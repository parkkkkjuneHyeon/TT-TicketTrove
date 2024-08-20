//package com.trove.ticket_trove.service.concert;
//
//import com.trove.ticket_trove.dto.concert.request.ConcertCreateRequest;
//import com.trove.ticket_trove.dto.concert.request.ConcertUpdateRequest;
//import com.trove.ticket_trove.dto.concert.response.ConcertInfoResponse;
//import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
//import com.trove.ticket_trove.model.storage.concert.ConcertRepository;
//import org.junit.jupiter.api.DynamicTest;
//import org.junit.jupiter.api.TestFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.DynamicTest.dynamicTest;
//
//@SpringBootTest
//class ConcertServiceDynamicTest {
//
//    @Autowired
//    private ConcertService concertService;
//    @Autowired
//    private ConcertRepository concertRepository;
//    @TestFactory
//    Stream<DynamicTest> concertServiceDynamicTest(){
//        List<ConcertInfoResponse> concertList = new ArrayList<>();
//        AtomicInteger count = new AtomicInteger(0);
//        return Stream.of(
//                dynamicTest("콘서트를 생성 후 조회를 통해 생성과 조회를 성공한다.", () -> {
//                    //given
//                    var concertCreateReq = new ConcertCreateRequest(
//                            "잠실 경기장",
//                            "나얼",
//                            LocalDateTime.of(2024,10,26,15,0),
//                            LocalDateTime.of(2024,10,26,22,0));
//                    var concertEntity = ConcertEntity.builder()
//                            .id(count.incrementAndGet())
//                            .concertName(concertCreateReq.concertName())
//                            .performer(concertCreateReq.performer())
//                            .showStart(concertCreateReq.showStart())
//                            .showEnd(concertCreateReq.showEnd())
//                            .build();
//
//                    //when
//                    //콘서트 생성
//                    concertService.addConcert(concertCreateReq);
//                    concertList.add(ConcertInfoResponse.from(concertEntity));
//                    //콘서트 조회
//                    var concertResponse = concertService
//                            .searchConcert((long) concertList.size());
//
//                    //then
//                    assertNotNull(concertResponse);
//                    assertEquals(concertList.get(count.get()-1).id(), concertResponse.id());
//                    assertEquals(concertList.get(count.get()-1).concertName(), concertResponse.concertName());
//                    assertEquals(concertList.get(count.get()-1).performer(), concertResponse.performer());
//                    assertEquals(concertList.get(count.get()-1).showStart(), concertResponse.showStart());
//                    assertEquals(concertList.get(count.get()-1).showEnd(), concertResponse.showEnd());
//                }),
//                dynamicTest("콘서트 전체 조회를 성공한다.", () -> {
//                    //given
//                    var concertCreateReq = new ConcertCreateRequest(
//                            "목동 경기장",
//                            "박효신",
//                            LocalDateTime.of(2024,12,25,15,0),
//                            LocalDateTime.of(2024,12,25,22,0));
//                    var concertEntity = ConcertEntity.builder()
//                            .id(count.incrementAndGet())
//                            .concertName(concertCreateReq.concertName())
//                            .performer(concertCreateReq.performer())
//                            .showStart(concertCreateReq.showStart())
//                            .showEnd(concertCreateReq.showEnd())
//                            .build();
//                    concertService.addConcert(concertCreateReq);
//                    concertList.add(ConcertInfoResponse.from(concertEntity));
//
//                    //when
//                    var concertResponses = concertService.searchConcerts();
//
//                    //then
//                    assertNotNull(concertResponses);
//                    assertEquals(concertList.size(), concertResponses.size());
//                    var concertArray = concertList.toArray();
//                    var responseArray = concertResponses.toArray();
//                    assertArrayEquals(concertArray, responseArray);
//                }),
//                dynamicTest("콘서트 수정을 성공한다.", () -> {
//                    //given
//                    var concertUpdateReq = new ConcertUpdateRequest(
//                            2L,
//                            "잠실 경기장",
//                            "박효신",
//                            LocalDateTime.of(2024,12,25,13,0),
//                            LocalDateTime.of(2024,12,25,21,0));
//                    var concert = concertList.get(concertUpdateReq.concertId().intValue() - 1);
//
//                    //when
//                    var concertInfoResponse = concertService.updateConcert(concertUpdateReq);
//
//                    //then
//                    assertNotNull(concertInfoResponse);
//                    assertEquals(concert.performer(), concertInfoResponse.performer());
//                    assertEquals(concert.id(), concertInfoResponse.id());
//
//                    assertNotEquals(concert.concertName(), concertInfoResponse.concertName());
//                    assertNotEquals(concert.showStart(), concertInfoResponse.showStart());
//                    assertNotEquals(concert.showEnd(), concertInfoResponse.showEnd());
//                    //수정된 데이터 저장
//                    concertList.remove(concert);
//                    concertList.add(ConcertInfoResponse.from(ConcertEntity.builder()
//                                    .id(concertUpdateReq.concertId())
//                                    .concertName(concertUpdateReq.concertName())
//                                    .performer(concertUpdateReq.performer())
//                                    .showStart(concertUpdateReq.showStart())
//                                    .showEnd(concertUpdateReq.showEnd())
//                                    .build())
//                    );
//                }),
//                dynamicTest("콘서트 정보를 소프트 삭제한다.", () -> {
//                    //when
//                    concertService.deleteConcert(2L);
//                    var responses = concertService.searchConcerts();
//                    //삭제된 콘서트 조회
//                    var deleteConcert = concertRepository.findByDeletedId(2L);
//
//                    //then
//                    assertNotNull(deleteConcert);
//                    assertNotEquals(concertList.size(), responses.size());
//                    concertList.removeLast();
//                })
//        );
//    }
//}