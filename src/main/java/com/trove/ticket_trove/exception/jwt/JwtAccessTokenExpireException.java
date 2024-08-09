package com.trove.ticket_trove.exception.jwt;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class JwtAccessTokenExpireException extends ClientErrorException {


    public JwtAccessTokenExpireException() {
        super(HttpStatus.UNAUTHORIZED, "토큰이 만료 됐습니다.");
    }
}
