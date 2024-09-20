package com.trove.ticket_trove;

import com.trove.ticket_trove.service.concert.ConcertReadService;
import com.trove.ticket_trove.service.redis.ConcertRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class TicketTroveApplication implements ApplicationRunner {
    private final ConcertReadService concertReadService;
    private final ConcertRedisService concertRedisService;

    public static void main(String[] args) {
        SpringApplication.run(TicketTroveApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        concertReadService.makeRedisDataWhenStartApplication();
        System.out.println(concertRedisService.printList());

    }
}