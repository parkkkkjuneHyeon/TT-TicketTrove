package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertRedisService {
    private final RedisTemplate<String, ConcertDetailsInfoResponse> redisTemplate;
    private final RedisTemplate<String, String> redisConcertIdTemplate;

    private static final List<String> concertIdList = new ArrayList<>();
    //key : id set

    public String key(Long concertId){
        return "Concert:"+ concertId;
    }

    //TODO 콘서트정보 삽입
    public void save(ConcertDetailsInfoResponse concertDetailsInfoResponse){
        redisTemplate.opsForValue().set(key(concertDetailsInfoResponse.concertId()), concertDetailsInfoResponse);
        concertIdList.add("Concert:"+concertDetailsInfoResponse.concertId().toString());
    }

    //TODO 콘서트정보 조회
    public ConcertDetailsInfoResponse getConcertInfo(Long concertId){
        return redisTemplate.opsForValue().get(key(concertId));
    }

    //TODO 콘서트 전체 조회
    public List<ConcertDetailsInfoResponse> getAllConcertInfo(){
        return redisTemplate.opsForValue().multiGet(concertIdList);
    }



    //TODO 콘서트정보 조회 후 있어야 바꿔서 삽입
    public ConcertDetailsInfoResponse updateConcertInfo(ConcertDetailsInfoResponse concertDetailsInfoResponse){
        Long concertId = concertDetailsInfoResponse.concertId();
        var concertInfo = getConcertInfo(concertId);
        if(concertInfo!=null){
            deleteConcertInfo(concertId);
            save(concertDetailsInfoResponse);
            return concertDetailsInfoResponse;
        }
        return null;
    }

    //TODO 삭제
    public void deleteConcertInfo(Long concertId){
        redisTemplate.delete(key(concertId));
        concertIdList.remove(concertId.toString());
    }
}
