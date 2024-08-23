package com.trove.ticket_trove.config.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.member.request.Member;
import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import com.trove.ticket_trove.model.entity.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    @Bean
    RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
    ) {
        var config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    RedisConnectionFactory replicaConnectionFactory(
            @Value("${spring.data.replica.host}") String host,
            @Value("${spring.data.replica.port}") int port
    ) {
        var config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }
    @Bean
    RedisConnectionFactory replica2ConnectionFactory(
            @Value("${spring.data.replica2.host}") String host,
            @Value("${spring.data.replica2.port}") int port
    ) {
        var config = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(config);
    }
    @Bean
    RedisTemplate<String, TicketDetailResponse> ticketTemplate(
            RedisConnectionFactory redisConnectionFactory
    ){
        return ticketRedisTemplate(redisConnectionFactory);
    }
    @Bean
    RedisTemplate<String, TicketDetailResponse> ticketReplicaTemplate(
            RedisConnectionFactory replicaConnectionFactory
    ){
        return ticketRedisTemplate(replicaConnectionFactory);
    }
    @Bean
    RedisTemplate<String, TicketDetailResponse> ticketReplica2Template(
            RedisConnectionFactory replica2ConnectionFactory
    ){
        return ticketRedisTemplate(replica2ConnectionFactory);
    }
    @Bean
    RedisTemplate<String, ConcertDetailsInfoResponse> concertTemplate(
            RedisConnectionFactory redisConnectionFactory
    ){
        return concertRedisTemplate(redisConnectionFactory);
    }
    @Bean
    RedisTemplate<String, ConcertDetailsInfoResponse> concertReplicaTemplate(
            RedisConnectionFactory replicaConnectionFactory
    ){
        return concertRedisTemplate(replicaConnectionFactory);
    }
    @Bean
    RedisTemplate<String, ConcertDetailsInfoResponse> concertReplica2Template(
            RedisConnectionFactory replica2ConnectionFactory
    ){
        return concertRedisTemplate(replica2ConnectionFactory);
    }
    //generic redisTemplate

    @Bean
    RedisTemplate<String, Member> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //기본 ObjectMapper를 사용 할 경우 시간입력에서 오류가 남
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

        var redisTemplate = new RedisTemplate<String, Member>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Member.class));
        return redisTemplate;

    }



    //ticketEntity redisTemplate

    RedisTemplate<String, TicketDetailResponse> ticketRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //기본 ObjectMapper를 사용 할 경우 시간입력에서 오류가 남
        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

        var redisTemplate = new RedisTemplate<String, TicketDetailResponse>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<TicketDetailResponse>(objectMapper, TicketDetailResponse.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<TicketDetailResponse>(objectMapper, TicketDetailResponse.class));
        return redisTemplate;
    }

    RedisTemplate<String, ConcertDetailsInfoResponse> concertRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        var objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

        var redisTemplate = new RedisTemplate<String, ConcertDetailsInfoResponse>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<ConcertDetailsInfoResponse>(objectMapper, ConcertDetailsInfoResponse.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<ConcertDetailsInfoResponse>(objectMapper, ConcertDetailsInfoResponse.class));
        return redisTemplate;
    }


}
