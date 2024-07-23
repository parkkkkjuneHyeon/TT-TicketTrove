package com.trove.ticket_trove.exception.member;

import com.trove.ticket_trove.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends ClientErrorException {

    public MemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "아이디나 비밀번호가 맞지 않습니다.");
    }

    public MemberNotFoundException(HttpStatus httpStatus, String email) {
        super(httpStatus, email);
    }
}
