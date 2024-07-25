package com.trove.ticket_trove.exception.seatgrade;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class SeatGradeNotFoundException extends ClientErrorException {

    public SeatGradeNotFoundException() {
        super(HttpStatus.NOT_FOUND, "현재 공연장에 있는 등급이 아닙니다.");
    }
    public SeatGradeNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
