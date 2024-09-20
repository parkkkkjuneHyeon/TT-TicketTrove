package com.trove.ticket_trove.service.auth.validation;

import com.trove.ticket_trove.exception.ClientErrorException;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class LoginValidation {

    public static void validateExistsEmail(
            String email,
            MemberRepository memberRepository) throws MemberExistsException {

        memberRepository.findByEmail(email).ifPresent(m -> {
            throw new MemberExistsException(email);
        });
    }
    public static void validatePassword(
            MemberEntity memberEntity,
            String password,
            BCryptPasswordEncoder bCryptPasswordEncoder) {

        if(!bCryptPasswordEncoder
                .matches(password, memberEntity.getPassword())) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST, "아이디나 비밀번호가 맞지 않습니다.");
        }
    }

}
