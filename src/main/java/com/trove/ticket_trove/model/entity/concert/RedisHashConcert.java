package com.trove.ticket_trove.model.entity.concert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("Concert")
public class RedisHashConcert {
    @Id
    private long id;

//    private String key;

    private String concertName;
    private String performer;
    private LocalDateTime showStart;
    private LocalDateTime showEnd;
    private LocalDateTime ticketingTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

//    public static String ofKey(ConcertEntity concertEntity) {
//        return concertEntity.getId()
//                +concertEntity.getConcertName()
//                +concertEntity.getPerformer()
//                +concertEntity.getShowStart();
//    }
//
//    public static String ofKey(Long id, String concertName, String performer, LocalDateTime showStart) {
//        return id+concertName+performer+showStart;
//    }

    public static RedisHashConcert from(ConcertEntity concertEntity) {
        return new RedisHashConcert(//ofKey(concertEntity),
                concertEntity.getId(),
                concertEntity.getConcertName(),
                concertEntity.getPerformer(),
                concertEntity.getShowStart(),
                concertEntity.getShowEnd(),
                concertEntity.getTicketingTime(),
                concertEntity.getCreatedAt(),
                concertEntity.getUpdatedAt(),
                concertEntity.getDeletedAt());
    }
}
