package com.trove.ticket_trove.exception.jwt;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class JwtRefreshTokenNotFoundException extends ClientErrorException {
    public JwtRefreshTokenNotFoundException() {
        super(HttpStatus.UNAUTHORIZED, "리프레시토큰이 없습니다. 다시 로그인 하세요.");
    }
}
