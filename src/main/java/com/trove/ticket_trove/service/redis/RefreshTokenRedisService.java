package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.member.response.MemberRefreshTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {
    private final RedisTemplate<String, MemberRefreshTokenDto> redisTemplate;

    private String key(String email) {
        return "member:" + email;
    }

    public void save(MemberRefreshTokenDto memberRefreshTokenDto) {
        //자동적으로 리프레시 토큰이 30분 뒤에 사라짐 또 만료 시간도 30분
        //디비에서도 스케줄링으로 JwtService에서 자동으로 만료된건 삭제됨.
        //쿠키에서도 30분으로 잡아주고
        //프론트에서 로그아웃하면 리프레시와 엑세스 토큰 둘다 삭제
        //로그인 할 시 리프레시토큰이 없다면 발급함.
        redisTemplate.opsForValue().set(
                key(memberRefreshTokenDto.email()),
                memberRefreshTokenDto,
                Duration.ofMinutes(30));
    }

    public void delete(String email) {
        redisTemplate.delete(key(email));
    }

    public MemberRefreshTokenDto get(String email) {
        return redisTemplate.opsForValue().get(key(email));
    }
}
