package com.trove.ticket_trove.config.jwt;

import com.trove.ticket_trove.service.jwt.JwtService;
import com.trove.ticket_trove.service.login.LoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final LoginService loginService;

    public JwtAuthenticationFilter(
            JwtService jwtService, LoginService loginService) {
        this.jwtService = jwtService;
        this.loginService = loginService;
    }

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
            String username = null;
            UserDetails userDetails = null;
            log.info("access token: {}", accessToken);
            if (!jwtService.checkExpireToken(accessToken)) {
                var refreshToken = findByRefreshTokenInCookie(request);
                log.info("Refresh token: " + refreshToken);
                jwtService.validationRefreshToken(refreshToken);
                username = jwtService.getUsername(refreshToken);
                userDetails = loginService.loadUserByUsername(username);
                accessToken = jwtService.generateToken(userDetails);

                Cookie accessTokenCookie =
                        new Cookie("accessToken", accessToken);
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(60 * 30);
                response.addCookie(accessTokenCookie);
                setAuthentication(userDetails);
            }else {
                username = jwtService.getUsername(accessToken);
                userDetails = loginService.loadUserByUsername(username);
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
    private String findByRefreshTokenInCookie(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }
}
