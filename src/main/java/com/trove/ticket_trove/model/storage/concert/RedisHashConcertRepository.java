package com.trove.ticket_trove.model.storage.concert;

import com.trove.ticket_trove.model.entity.concert.RedisHashConcert;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashConcertRepository extends CrudRepository<RedisHashConcert, String> {
}
