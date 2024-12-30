package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.member.response.Member;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberRedisService {
    private final RedisTemplate<String, Member> memberTemplate;
    private final RedisTemplate<String, Member> memberReplicaTemplate;

    public String key(String email) {
        return "Email:" + email;
    }

    public void save(Member member) {
        memberTemplate.opsForValue().set(key(member.getEmail()), member, Duration.ofMinutes(30));
    }

    public Member get(String email) {
        return memberReplicaTemplate.opsForValue().get(key(email));
    }

    public void delete(String email) {
        if(Boolean.TRUE.equals(memberTemplate.delete(key(email))))
            log.info("Deleted email: ", email);
        else
            throw new MemberNotFoundException(
                    HttpStatus.BAD_REQUEST,
                    "이미 삭제됐거나 없는 회원정보입니다.");
    }
}