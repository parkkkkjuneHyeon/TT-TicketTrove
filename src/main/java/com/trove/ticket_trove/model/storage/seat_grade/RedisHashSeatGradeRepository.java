package com.trove.ticket_trove.model.storage.seat_grade;

import com.trove.ticket_trove.model.entity.seat_grade.RedisHashSeatGrade;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashSeatGradeRepository extends CrudRepository<RedisHashSeatGrade, String> {
}
