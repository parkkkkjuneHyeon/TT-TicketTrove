package com.trove.ticket_trove.service.concert.validation;

import com.trove.ticket_trove.exception.concert.ConcertExistsException;
import com.trove.ticket_trove.model.storage.concert.ConcertRepository;

import java.time.LocalDateTime;

public class ConcertValidation {

    public static void validateConcert(
            String concertName,
            String performer,
            LocalDateTime showStart,
            ConcertRepository concertRepository
    ) {
        concertRepository.findByConcertNameAndPerformerAndShowStart(
                        concertName, performer, showStart)
                .ifPresent(concert -> {
                    throw new ConcertExistsException(concertName, performer, showStart);
                });
    }
}
