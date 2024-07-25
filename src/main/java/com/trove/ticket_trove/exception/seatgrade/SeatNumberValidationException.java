package com.trove.ticket_trove.exception.seatgrade;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class SeatNumberValidationException extends ClientErrorException {
    public SeatNumberValidationException() {
        super(HttpStatus.BAD_REQUEST, "현재 등급의 좌석번호와 맞지 않습니다.");
    }
}
