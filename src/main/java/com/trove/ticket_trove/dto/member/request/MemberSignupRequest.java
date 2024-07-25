package com.trove.ticket_trove.dto.member.request;

import jakarta.validation.constraints.*;

public record MemberSignupRequest(
        @NotBlank
        String name,
        @Email
        String email,
        String password,
        @Size(min = 1, max = 1)
        String gender,
        @Min(0)
        @Max(150)
        Integer age
) {

}
