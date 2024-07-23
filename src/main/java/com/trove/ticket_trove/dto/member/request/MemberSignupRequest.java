package com.trove.ticket_trove.dto.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
    String name,
    @Email
    String email,
    String password,
    @Size(min = 1, max = 1)
    String gender,
    @Min(0)
    @Max(150)
    Integer age) {

}
