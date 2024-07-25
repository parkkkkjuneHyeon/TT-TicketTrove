package com.trove.ticket_trove.exception.ticket;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class TicketExistsException extends ClientErrorException {

    public TicketExistsException() {
        super(HttpStatus.BAD_REQUEST, "티켓이 존재합니다.");
    }

}
