package com.trove.ticket_trove.exception;

import com.trove.ticket_trove.exception.concert.ConcertExistsException;
import com.trove.ticket_trove.exception.dto.ClientErrorResponse;
import com.trove.ticket_trove.exception.member.MemberExistsException;
import com.trove.ticket_trove.exception.member.MemberNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MemberExistsException.class)
    public ResponseEntity<ClientErrorResponse> memberExistsException(
            MemberExistsException e) {
        return new ResponseEntity<>(
                new ClientErrorResponse(
                        e.getHttpStatus(),
                        e.getMessage()),
                        e.getHttpStatus());
    }
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ClientErrorResponse> memberNotFoundException(
            MemberNotFoundException e) {
        return new ResponseEntity<>(
                new ClientErrorResponse(
                        e.getHttpStatus(),
                        e.getMessage()),
                e.getHttpStatus()
        );
    }
    @ExceptionHandler(ConcertExistsException.class)
    public ResponseEntity<ClientErrorResponse> concertExistsException(ConcertExistsException e) {
        return new ResponseEntity<>(
                new ClientErrorResponse(e.getHttpStatus(),
                        e.getMessage()),
                e.getHttpStatus()
        );
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ClientErrorResponse> clientErrorException(
            ClientErrorException e) {
        return new ResponseEntity<>(
                new ClientErrorResponse(
                        e.getHttpStatus(),
                        e.getMessage()),
                e.getHttpStatus());
    }
}
