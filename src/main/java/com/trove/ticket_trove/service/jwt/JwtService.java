package com.trove.ticket_trove.service.jwt;

import com.trove.ticket_trove.dto.member.response.MemberRefreshTokenDto;
import com.trove.ticket_trove.exception.jwt.JwtRefreshTokenExpireException;
import com.trove.ticket_trove.exception.jwt.JwtRefreshTokenNotFoundException;
import com.trove.ticket_trove.model.entity.jwt.RefreshTokenEntity;
import com.trove.ticket_trove.model.storage.jwt.RefreshTokenRepository;
import com.trove.ticket_trove.service.redis.RefreshTokenRedisService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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
    private final RefreshTokenRedisService refreshTokenRedisService;

    public JwtService(
            @Value("${jwt.secret-key}") String key,
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenRedisService refreshTokenRedisService) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenRedisService = refreshTokenRedisService;
    }

    public String generateToken(UserDetails userDetails) {
        var now = new Date();
        var expire = new Date(now.getTime() + 1000 * 60);
        return createToken(
                userDetails.getUsername(),
                expire, now);
    }

    @Transactional
    public String generateRefreshToken(UserDetails userDetails) {
        var now = new Date();
        var expire = new Date(now.getTime() + 1000 * 60 * 30);
        var refreshToken =  createToken(
                userDetails.getUsername(),
                expire, now);
        var refreshTokenEntity = RefreshTokenEntity.builder()
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        refreshTokenRedisService.save(
                MemberRefreshTokenDto.from(
                        userDetails.getUsername(),
                        refreshToken)
        );
        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRedisService.delete(getEmail(refreshToken));
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    @Transactional
    public boolean validationRefreshToken(String refreshToken) {
        if (refreshToken == null)
            return false;
        //리프레쉬 토큰 존재하는지 체크
        checkExistsRefreshToken(refreshToken);
        //만료 체크
        if(!checkExpireToken(refreshToken)) {
            deleteRefreshToken(refreshToken);
            throw new JwtRefreshTokenExpireException();
        }
        return true;
    }

    private void checkExistsRefreshToken(String refreshToken) {

        String email = getEmail(refreshToken);
        var memberRefreshTokenDto = refreshTokenRedisService
                .get(email);

        if (memberRefreshTokenDto != null)
            return;

        var refreshEntity = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(JwtRefreshTokenNotFoundException::new);
        refreshTokenRedisService
                .save(MemberRefreshTokenDto.from(
                        email,
                        refreshEntity.getRefreshToken()));
    }
    //5분마다 만료된 토큰은 삭제
    @Scheduled(cron = "0 */5 * * * ?")
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredToken(new Date().getTime());
    }

    public String getEmail(String accessToken) {
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
            if (accessToken == null)
                throw new JwtRefreshTokenNotFoundException();

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
