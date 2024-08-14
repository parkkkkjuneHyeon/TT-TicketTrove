package com.trove.ticket_trove.model.storage.ticket;

import com.trove.ticket_trove.model.entity.ticket.RedisHashTicket;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashTicketRepository extends CrudRepository<RedisHashTicket, String> {

}
