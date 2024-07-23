package com.trove.ticket_trove.exception.dto;

import org.springframework.http.HttpStatus;

public record ClientErrorResponse(HttpStatus httpStatus, String message) {
}
