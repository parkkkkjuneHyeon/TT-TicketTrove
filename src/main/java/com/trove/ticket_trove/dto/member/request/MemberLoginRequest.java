package com.trove.ticket_trove.dto.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password) {
}
