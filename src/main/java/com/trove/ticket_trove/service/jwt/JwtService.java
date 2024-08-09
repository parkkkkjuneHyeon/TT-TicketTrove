package com.trove.ticket_trove.service.jwt;

import com.trove.ticket_trove.exception.jwt.JwtRefreshTokenExpireException;
import com.trove.ticket_trove.exception.jwt.JwtRefreshTokenNotFoundException;
import com.trove.ticket_trove.model.entity.jwt.RefreshTokenEntity;
import com.trove.ticket_trove.model.storage.jwt.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final RefreshTokenRepository refreshTokenRepository;
    public JwtService(
            @Value("${jwt.secret-key}") String key,
            RefreshTokenRepository refreshTokenRepository) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateToken(UserDetails userDetails) {
        var now = new Date();
        var expire = new Date(now.getTime() + 1000 * 5);
        return createToken(
                userDetails.getUsername(),
                expire, now);
    }
    @Transactional
    public String generateRefreshToken(UserDetails userDetails) {
        var now = new Date();
        var expire = new Date(now.getTime() + 1000 * 60 * 24 * 7);
        var refreshToken =  createToken(
                userDetails.getUsername(),
                expire, now);
        var refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    public boolean validationRefreshToken(String refreshToken) {
        if (refreshToken == null)
            return false;
        if(!checkExpireToken(refreshToken)) {
            throw new JwtRefreshTokenExpireException();
        }
        checkExistsRefreshToken(refreshToken);
        return true;
    }

    private void checkExistsRefreshToken(String refreshToken) {
        refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(JwtRefreshTokenNotFoundException::new);
    }
    //30초마다 만료된 토큰은 삭제
    @Scheduled(cron = "*/30 * * * * ?")
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredToken(new Date().getTime());
    }


    public String getUsername(String accessToken) {
        return getSubject(accessToken);
    }

    private String createToken(
            String username,
            Date expire,
            Date now) {

        return Jwts.builder()
                .signWith(key)
                .subject(username)
                .issuedAt(now)
                .expiration(expire)
                .compact();
    }

    private String getSubject(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload()
                    .getSubject();

        }catch (JwtException e) {
            throw new JwtException("유효하지 않는 토큰입니다.");
        }
    }
    //만료 됐으면 true
    public boolean checkExpireToken(String token) {
        return validateExpireToken(token);
    }

    private boolean validateExpireToken(String token) {
        try {
            if(token == null)
                return false;
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return true;
        }catch (JwtException e) {
            return false;
        }

    }
}
