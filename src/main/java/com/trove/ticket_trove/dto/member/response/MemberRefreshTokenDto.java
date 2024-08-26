package com.trove.ticket_trove.dto.member.response;


public record MemberRefreshTokenDto(
        String email,
        String refreshToken) {

    public static MemberRefreshTokenDto from(
            String email,
            String refreshToken) {

        return new MemberRefreshTokenDto(
                email,
                refreshToken);
    }
}
