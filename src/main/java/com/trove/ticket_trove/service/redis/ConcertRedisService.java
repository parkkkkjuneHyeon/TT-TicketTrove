package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.service.concert.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ConcertRedisService {
    private final RedisTemplate<String, ConcertDetailsInfoResponse> concertTemplate;
    private final RedisTemplate<String, ConcertDetailsInfoResponse> concertReplicaTemplate;
    private final RedisTemplate<String, ConcertDetailsInfoResponse> concertReplica2Template;

    private static final Set<String> concertIdList = new HashSet<>();

    public String printList() {
        return concertIdList.toString();
    }


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    //key : id set

    public String key(Long concertId) {
        return "Concert:" + concertId;
    }
    public String keyList(Long concertId) {
        return "ConcertList:" + concertId;
    }

    //TODO 자주 검색하는 목록 찾기
    public List<ConcertDetailsInfoResponse> getConcertListForReservation() {
        return concertReplica2Template.opsForValue().multiGet(concertIdList).stream().filter(Objects::nonNull).toList();
    }

    public void saveList(ConcertDetailsInfoResponse concertDetailsInfoResponse) {
        concertTemplate.opsForValue().set(keyList(concertDetailsInfoResponse.concertId()), concertDetailsInfoResponse, Duration.ofMinutes(180));
        concertIdList.add((keyList(concertDetailsInfoResponse.concertId())));
        scheduler.schedule(() -> {
            deleteKeyFromSet(concertDetailsInfoResponse.concertId());
        }, 180, TimeUnit.MINUTES);
    }

    //TODO 콘서트정보 삽입

    public ConcertDetailsInfoResponse save(ConcertDetailsInfoResponse concertDetailsInfoResponse) {
        concertTemplate.opsForValue().set(key(concertDetailsInfoResponse.concertId()), concertDetailsInfoResponse, Duration.ofMinutes(30));
        return concertDetailsInfoResponse;
    }

    //TODO 콘서트정보 조회
    public ConcertDetailsInfoResponse getConcertInfo(Long concertId) {
        return concertReplicaTemplate.opsForValue().get(key(concertId));
    }

//    //TODO 콘서트 전체 조회
//    public List<ConcertDetailsInfoResponse> getAllConcertInfo() {
//
//        return concertReplica2Template.opsForValue().multiGet(concertIdList)
//                .stream().filter(Objects::nonNull).toList();

//    Scheduler VS 검색 하면서 키 삭제하기 (어떤게 프로그램 속도와 과부하에 도움이 되는가?)

//    return concertIdList.stream().map(key -> {
//        var concertInfo = concertReplicaTemplate.opsForValue().get(key);
//        if(concertInfo == null){
//            deleteKeyFromSet(key.split(":")[1]);
//        }
//        return concertInfo;
//    }).filter(Objects::nonNull).toList();
//   }


    //TODO 콘서트정보 조회 후 있어야 바꿔서 삽입
    public ConcertDetailsInfoResponse updateConcertInfo(ConcertDetailsInfoResponse concertDetailsInfoResponse) {
        Long concertId = concertDetailsInfoResponse.concertId();
        var concertInfo = getConcertInfo(concertId);
        if (concertInfo != null) {
            deleteConcertInfo(concertId);
            save(concertDetailsInfoResponse);
            return concertDetailsInfoResponse;
        }
        return null;
    }

    //TODO 삭제
    public void deleteConcertInfo(Long concertId) {
        concertTemplate.delete(key(concertId));
        concertIdList.remove(concertId.toString());
    }

    public void deleteKeyFromSet(Long concertId) {
        concertIdList.remove(concertId.toString());
    }

}
