package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.member.response.Member;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberRedisService {
    private final RedisTemplate<String, Member> memberTemplate;
    private final RedisTemplate<String, Member> memberReplicaTemplate;
    private final RedisTemplate<String, Member> memberReplica2Template;

    public String key(String email) {
        return "Email:" + email;
    }

    public void save(Member member) {
        memberTemplate.opsForValue().set(key(member.getEmail()), member, Duration.ofMinutes(30));
    }

    public Member get(String email) {
        return memberReplicaTemplate.opsForValue().get(key(email));
    }
}