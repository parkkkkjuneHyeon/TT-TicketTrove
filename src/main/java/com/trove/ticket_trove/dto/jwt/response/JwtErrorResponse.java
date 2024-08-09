package com.trove.ticket_trove.dto.jwt.response;

import org.springframework.http.HttpStatus;

public record JwtErrorResponse(HttpStatus httpStatus, String message) {
}
