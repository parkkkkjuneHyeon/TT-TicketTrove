package com.trove.ticket_trove.model.storage.concert;

import com.trove.ticket_trove.model.entity.concert.ConcertEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, Long> {
    @Query("SELECT c FROM ConcertEntity c " +
            "WHERE c.concertName = :concertName " +
            "AND c.performer = :performer " +
            "AND c.showStart = :showStart " +
            "AND c.deletedAt IS NULL")
    Optional<ConcertEntity> findByConcertNameAndPerformerAndShowStart(
            @Param("concertName") String concertName,
            @Param("performer") String performer,
            @Param("showStart") LocalDateTime showStart);

    @Query(value = "SELECT * FROM concert c WHERE id = :concertId AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<ConcertEntity> findByDeletedId(@Param("concertId") Long concertId);

    List<ConcertEntity> findAllByOrderByShowStartAsc(Pageable pageable);


    @Query(value = "SELECT * FROM concert WHERE ticketing_time BETWEEN :now - INTERVAL 180 MINUTE AND :now + INTERVAL 30 MINUTE" , nativeQuery = true)
    List<ConcertEntity> findConcertEntityOrderByTicketingTimeAsc(@Param("now") LocalDateTime now);
}
