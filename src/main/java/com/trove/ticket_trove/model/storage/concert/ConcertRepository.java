package com.trove.ticket_trove.model.storage.concert;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, Long> {
    @Query("SELECT c FROM ConcertEntity c WHERE c.concertName = :concertName AND c.performer = :performer AND c.deletedAt IS NULL")
    Optional<ConcertEntity> findByConcertNameAndPerformer(@Param("concertName") String concertName, @Param("performer") String performer);

}
