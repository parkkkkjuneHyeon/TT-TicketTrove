package com.trove.ticket_trove.service.login;

import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.exception.ClientErrorException;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final MemberRepository memberRepository;

    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    //회원가입
    @Transactional
    public MemberEntity signup(MemberSignupRequest request) {
        validateExistsEmail(request.email());
        var memberEntity = MemberEntity.from(
                request.name(),
                request.email(),
                request.password(),
                request.gender(),
                request.age());
        return memberRepository.save(memberEntity);
    }
    //로그인
    public MemberEntity authenticate(MemberLoginRequest request) {
        var memberEntity = memberRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new MemberNotFoundException());
        validatePassword(memberEntity, request);
        return memberEntity;
    }
    private void validatePassword(
            MemberEntity memberEntity,
            MemberLoginRequest request) {
        if(!memberEntity.getPassword().equals(request.password())) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST, "아이디나 비밀번호가 맞지 않습니다.");
        }
    }

    private void validateExistsEmail(String email) {
        memberRepository.findByEmail(email).ifPresent(m -> {
                throw new MemberExistsException(email);
        });
    }
}
