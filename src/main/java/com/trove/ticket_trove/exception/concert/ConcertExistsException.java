package com.trove.ticket_trove.exception.concert;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ConcertExistsException extends ClientErrorException {


    public ConcertExistsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ConcertExistsException(String concertName, String performer) {
        super(HttpStatus.BAD_REQUEST,
                performer + "님의 콘서트장 " +
                concertName + "는(은) " +
                "이미 일정을 예약 되었습니다. ");
    }
}
