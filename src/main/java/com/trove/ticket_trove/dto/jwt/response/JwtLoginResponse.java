package com.trove.ticket_trove.dto.jwt.response;

public record JwtLoginResponse(
        Long memberId,
        String accessToken,
        String role) {
}
