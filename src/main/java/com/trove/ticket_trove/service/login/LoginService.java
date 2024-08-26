package com.trove.ticket_trove.service.login;

import com.trove.ticket_trove.dto.jwt.response.JwtLoginResponse;
import com.trove.ticket_trove.dto.member.request.MemberAdminSignupRequest;
import com.trove.ticket_trove.dto.member.request.MemberDeleteRequest;
import com.trove.ticket_trove.dto.member.request.MemberLoginRequest;
import com.trove.ticket_trove.dto.member.request.MemberSignupRequest;
import com.trove.ticket_trove.exception.ClientErrorException;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.storage.member.MemberRepository;
import com.trove.ticket_trove.model.user.Role;
import com.trove.ticket_trove.service.jwt.JwtService;
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
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final CookieUtilService cookieUtilService;

    //유저회원가입
    @Transactional
    public void signup(MemberSignupRequest request) {
        validateExistsEmail(request.email());
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
        validateExistsEmail(request.email());
        var memberEntity = MemberEntity.from(
                request.name(),
                request.email(),
                bCryptPasswordEncoder.encode(request.password()),
                request.gender(),
                request.age(),
                Role.ADMIN);
        memberRepository.save(memberEntity);
    }

    //로그인
    public JwtLoginResponse authenticate(
            MemberLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        var memberEntity = getMemberEntity(request.email());
        validatePassword(memberEntity, request.password());

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

    @Transactional
    public void deleteMember(MemberDeleteRequest request) {
        memberRepository.deleteByEmail(request.email());
    }

    private void validatePassword(
            MemberEntity memberEntity,
            String password) {
        if(!bCryptPasswordEncoder
                .matches(password, memberEntity.getPassword())) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST, "아이디나 비밀번호가 맞지 않습니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getMemberEntity(email);
    }

    private void validateExistsEmail(String email) {
        memberRepository.findByEmail(email).ifPresent(m -> {
                throw new MemberExistsException(email);
        });
    }

    private MemberEntity getMemberEntity(String email) {
        return memberRepository
                .findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }
}
