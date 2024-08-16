package com.trove.ticket_trove.exception.seatgrade;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class SeatGradeExistsException extends ClientErrorException {

    public SeatGradeExistsException() {
        super(HttpStatus.BAD_REQUEST, "현재 있는 등급 및 좌석입니다.");
    }
    public SeatGradeExistsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
