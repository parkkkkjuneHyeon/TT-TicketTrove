package com.trove.ticket_trove.service.auth;

import com.trove.ticket_trove.dto.jwt.response.JwtLoginResponse;
import com.trove.ticket_trove.dto.member.request.MemberAdminSignupRequest;
import com.trove.ticket_trove.dto.member.request.MemberDeleteRequest;
import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.dto.member.response.Member;
import com.trove.ticket_trove.exception.ClientErrorException;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import com.trove.ticket_trove.model.user.Role;
import com.trove.ticket_trove.service.auth.validation.LoginValidation;
import com.trove.ticket_trove.service.redis.MemberRedisService;
import com.trove.ticket_trove.util.CookieUtilService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginReadService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final CookieUtilService cookieUtilService;
    private final MemberRedisService memberRedisService;

    //로그인
    public JwtLoginResponse authenticate(
            MemberLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        var memberEntity = getMemberEntity(request.email());

        LoginValidation.validatePassword(
                memberEntity,
                request.password(),
                bCryptPasswordEncoder);

        var accessToken = jwtService.generateToken(memberEntity);
        var refreshToken = cookieUtilService
                .findByCookieInCookie(servletRequest, "refreshToken");

        if (!jwtService.checkExpireToken(refreshToken)) {
            refreshToken = jwtService.generateRefreshToken(memberEntity);
            Cookie cookie = cookieUtilService.createCookie(
                    "refreshToken", refreshToken, 60 * 30);
            servletResponse.addCookie(cookie);
        }

        return new JwtLoginResponse(
                memberEntity.getId(),
                accessToken,
                memberEntity.getRole().name());
    }

    @Transactional
    public void logout(
            HttpServletRequest servletRequest) {
        String refreshToken = cookieUtilService
                .findByCookieInCookie(servletRequest, "refreshToken");
        jwtService.deleteRefreshToken(refreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getMemberEntity(email);
    }

    private MemberEntity getMemberEntity(String email) {
        var member = memberRedisService.get(email);
        if(member != null) {
            return MemberEntity.from(member);
        }
        MemberEntity memberEntity = memberRepository
                .findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        memberRedisService.save(Member.from(memberEntity));
        return memberEntity;
    }
}
