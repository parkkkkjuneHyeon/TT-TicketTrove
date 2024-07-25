package com.trove.ticket_trove.exception.ticket;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends ClientErrorException {

    public TicketNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 티켓이 없습니다.");
    }

    public TicketNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
