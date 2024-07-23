package com.trove.ticket_trove.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ClientErrorException extends RuntimeException {
    private final HttpStatus httpStatus;
    public ClientErrorException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    public ClientErrorException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
