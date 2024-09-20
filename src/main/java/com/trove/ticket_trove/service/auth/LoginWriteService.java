package com.trove.ticket_trove.service.auth;

import com.trove.ticket_trove.dto.member.request.MemberAdminSignupRequest;
import com.trove.ticket_trove.dto.member.request.MemberDeleteRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import com.trove.ticket_trove.model.user.Role;
import com.trove.ticket_trove.service.auth.validation.LoginValidation;
import com.trove.ticket_trove.service.redis.MemberRedisService;
import com.trove.ticket_trove.util.CookieUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginWriteService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRedisService memberRedisService;

    //유저회원가입
    @Transactional
    public void signup(MemberSignupRequest request) {
        LoginValidation.validateExistsEmail(request.email(), memberRepository);
        var memberEntity = MemberEntity.from(
                request.name(),
                request.email(),
                bCryptPasswordEncoder.encode(request.password()),
                request.gender(),
                request.age(),
                Role.USER);
        memberRepository.save(memberEntity);
    }

    //관리자회원가입
    @Transactional
    public void adminSignup(
            MemberAdminSignupRequest request) {
        LoginValidation.validateExistsEmail(request.email(), memberRepository);
        var memberEntity = MemberEntity.from(
                request.name(),
                request.email(),
                bCryptPasswordEncoder.encode(request.password()),
                request.gender(),
                request.age(),
                Role.ADMIN);
        memberRepository.save(memberEntity);
    }

    @Transactional
    public void deleteMember(MemberDeleteRequest request) {
        memberRedisService.delete(request.email());
        memberRepository.deleteByEmail(request.email());
    }
}
