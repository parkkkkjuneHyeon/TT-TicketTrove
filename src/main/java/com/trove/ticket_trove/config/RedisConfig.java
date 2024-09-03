package com.trove.ticket_trove.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trove.ticket_trove.dto.concert.response.ConcertDetailsInfoResponse;
import com.trove.ticket_trove.dto.member.response.Member;
import com.trove.ticket_trove.dto.member.response.MemberRefreshTokenDto;
import com.trove.ticket_trove.dto.ticket.response.TicketDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@Slf4j
public class RedisConfig {

    private final ObjectMapper redisObjectMapper;

    public RedisConfig() {
        this.redisObjectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    }

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

    @Bean
    RedisTemplate<String, Member> memberTemplate(
            RedisConnectionFactory redisConnectionFactory
    ){
        return memberRedisTemplate(redisConnectionFactory);
    }
    @Bean
    RedisTemplate<String, Member> memberReplicaTemplate(
            RedisConnectionFactory replicaConnectionFactory
    ){
        return memberRedisTemplate(replicaConnectionFactory);
    }
    @Bean
    RedisTemplate<String, Member> memberReplica2Template(
            RedisConnectionFactory replica2ConnectionFactory
    ){
        return memberRedisTemplate(replica2ConnectionFactory);
    }

    //generic redisTemplate
    @Bean
    RedisTemplate<String, MemberRefreshTokenDto> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, MemberRefreshTokenDto>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, MemberRefreshTokenDto.class));
        return redisTemplate;

    }

    //ticketEntity redisTemplate
    RedisTemplate<String, TicketDetailResponse> ticketRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, TicketDetailResponse>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, TicketDetailResponse.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, TicketDetailResponse.class));
        return redisTemplate;
    }

    RedisTemplate<String, ConcertDetailsInfoResponse> concertRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, ConcertDetailsInfoResponse>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, ConcertDetailsInfoResponse.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, ConcertDetailsInfoResponse.class));
        return redisTemplate;
    }

    RedisTemplate<String, Member> memberRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Member>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, Member.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(redisObjectMapper, Member.class));
        return redisTemplate;
    }


}
