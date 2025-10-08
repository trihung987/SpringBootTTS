package me.trihung.repository;

import me.trihung.entity.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    Optional<CheckIn> findByCheckInCode(String checkInCode);

    List<CheckIn> findByOrderId(UUID orderId);

    @Query("SELECT c FROM CheckIn c " +
           "JOIN FETCH c.zone z " +
           "JOIN FETCH z.event e " +
           "WHERE e.id = :eventId")
    Page<CheckIn> findAllByEventId(@Param("eventId") UUID eventId, Pageable pageable);

    @Query("SELECT c FROM CheckIn c " +
           "JOIN FETCH c.zone z " +
           "JOIN FETCH z.event e " +
           "WHERE e.id = :eventId AND c.status = :status")
    Page<CheckIn> findAllByEventIdAndStatus(@Param("eventId") UUID eventId, 
                                            @Param("status") me.trihung.enums.CheckInStatus status, 
                                            Pageable pageable);

    @Query("SELECT c FROM CheckIn c " +
           "JOIN FETCH c.zone z " +
           "WHERE z.id = :zoneId")
    Page<CheckIn> findAllByZoneId(@Param("zoneId") UUID zoneId, Pageable pageable);
}