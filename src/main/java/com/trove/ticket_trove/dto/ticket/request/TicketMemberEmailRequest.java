package com.trove.ticket_trove.dto.ticket.request;

import jakarta.validation.constraints.NotBlank;

public record TicketMemberEmailRequest(
        @NotBlank
        String email
) {
}
