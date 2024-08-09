package com.trove.ticket_trove.exception.jwt;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class JwtRefreshTokenExpireException extends ClientErrorException {
    public JwtRefreshTokenExpireException() {
        super(HttpStatus.UNAUTHORIZED, "리프레시토큰이 만료 됐습니다. 다시 로그인 하세요.");
    }
}
