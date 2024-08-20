package com.trove.ticket_trove.exception.concert;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ConcertExistsException extends ClientErrorException {


    public ConcertExistsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ConcertExistsException(
            String concertName,
            String performer,
            LocalDateTime showStart) {
        super(HttpStatus.BAD_REQUEST,
                performer + "님의 콘서트장 " +
                concertName + "는(은) "
                + "%s년 %s월 %s일 %s시 %s분".formatted(
                        showStart.getYear(),
                        showStart.getMonth(),
                        showStart.getDayOfMonth(),
                        showStart.getHour(),
                        showStart.getMinute()) +" 일정으로"
                + "이미 예약 되었습니다. ");
    }
}
