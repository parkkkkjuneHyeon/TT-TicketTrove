package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.member.request.Member;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberRedisService {
    private final RedisTemplate<String, Member> redisTemplate;

    private String key(String email){
        return "member:" + email;
    }

    public void save(MemberEntity memberEntity){
        redisTemplate.opsForValue().set(key(memberEntity.getEmail()), Member.from(memberEntity));
    }

    public void delete(String email){
        redisTemplate.delete(key(email));
    }

    public MemberEntity get(String email){
        return MemberEntity.from(redisTemplate.opsForValue().get(key(email)));
    }

}
