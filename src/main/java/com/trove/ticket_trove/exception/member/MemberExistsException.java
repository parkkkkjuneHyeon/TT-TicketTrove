package com.trove.ticket_trove.exception.member;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class MemberExistsException extends ClientErrorException {

    public MemberExistsException() {
        super(HttpStatus.BAD_REQUEST, "Member already exists");
    }
    public MemberExistsException(String email) {
        super(HttpStatus.BAD_REQUEST, "email : " + email + " already exists");
    }

    public MemberExistsException(String email, HttpStatus httpStatus) {
        super(httpStatus, email);
    }
}
