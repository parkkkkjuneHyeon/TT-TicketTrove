package com.trove.ticket_trove.model.storage.member;

import com.trove.ticket_trove.model.entity.member.RedisHashMember;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashMemberRepository extends CrudRepository<RedisHashMember, String> {
}
