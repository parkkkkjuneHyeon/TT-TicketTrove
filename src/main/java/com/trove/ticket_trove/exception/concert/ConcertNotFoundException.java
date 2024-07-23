package com.trove.ticket_trove.exception.concert;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ConcertNotFoundException extends ClientErrorException {

    public ConcertNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 콘서트장이 없습니다.");
    }

    public ConcertNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
