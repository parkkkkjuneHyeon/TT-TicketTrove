package com.trove.ticket_trove.service.redis;

import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import com.trove.ticket_trove.model.entity.ticket.TicketEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketRedisService {
    private final RedisTemplate<String, TicketDetailResponse> ticketTemplate;
    private final RedisTemplate<String, TicketDetailResponse> ticketReplicaTemplate;
    private final RedisTemplate<String, TicketDetailResponse> ticketReplica2Template;

    public static String key(Long concertId){
        return "Ticket:Concert:" + concertId;
    }


    public static String subKey(TicketEntity ticketEntity){
        return "key:"
                + ticketEntity.getMemberEmail().getEmail()
                + ticketEntity.getSeatGrade().getGrade()
                + ticketEntity.getSeatNumber();
    }

    public static String subKey(String email, String seatGrade, Integer seatNumber){
        return "key:"+email+seatGrade+seatNumber;
    }

    public void save(TicketEntity ticketEntity){
        ticketTemplate.opsForHash().put(key(ticketEntity.getConcertId().getId()),
                subKey(ticketEntity),
                TicketDetailResponse.from(ticketEntity));
        ticketTemplate.expire(key(ticketEntity.getConcertId().getId()), Duration.ofMinutes(180L));
    }

    public Map<Object, Object> getTicketList(String key){
        return ticketReplicaTemplate.opsForHash().entries(key);
    }

    public TicketDetailResponse getTicketInfo(String key, String subKey){
        return (TicketDetailResponse) ticketReplica2Template.opsForHash().get(key, subKey);
    }

    public void delete(String key, String subKey){
       ticketTemplate.opsForHash().delete(key, subKey);
    }
}
