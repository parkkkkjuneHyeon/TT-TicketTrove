package com.trove.ticket_trove.config.jwt;

import com.trove.ticket_trove.service.jwt.JwtService;
import com.trove.ticket_trove.service.login.LoginService;
import com.trove.ticket_trove.util.CookieUtilService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final LoginService loginService;
    private final CookieUtilService cookieUtilService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var BEARER = "Bearer ";

        if (authorizationHeader != null
                && authorizationHeader.startsWith(BEARER)
                && authentication == null) {

            String accessToken = authorizationHeader.substring(BEARER.length());
            String email = null;
            UserDetails userDetails = null;
            log.info("access token: {}", accessToken);
            if (!jwtService.checkExpireToken(accessToken)) {
                var refreshToken = cookieUtilService
                        .findByCookieInCookie(request, "refreshToken");
                jwtService.validationRefreshToken(refreshToken);
                email = jwtService.getEmail(refreshToken);

                log.info("Refresh token: {}", refreshToken);
                userDetails = loginService.loadUserByUsername(email);
                accessToken = jwtService.generateToken(userDetails);

                Cookie accessTokenCookie = cookieUtilService.createCookie(
                        "accessToken", accessToken, 60 * 3);
                response.addCookie(accessTokenCookie);
                setAuthentication(userDetails);
            }else {
                email = jwtService.getEmail(accessToken);
                userDetails = loginService.loadUserByUsername(email);
                setAuthentication(userDetails);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(UserDetails userDetails) {
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
