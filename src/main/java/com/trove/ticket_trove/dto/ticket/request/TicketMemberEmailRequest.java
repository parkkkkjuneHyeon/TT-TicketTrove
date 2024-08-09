package com.trove.ticket_trove.dto.ticket.request;

import jakarta.validation.constraints.NotBlank;

@Deprecated
public record TicketMemberEmailRequest(
        @NotBlank
        String email
) {
}
