package me.trihung.repository;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.trihung.entity.Event;
import me.trihung.entity.User;
import me.trihung.enums.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
	Page<Event> findByStatus(EventStatus status, Pageable pageable);

	Page<Event> findByEventNameContainingIgnoreCase(String search, Pageable pageable);

	Page<Event> findByStatusAndEventNameContainingIgnoreCase(EventStatus status, String search, Pageable pageable);

	Page<Event> findByOwner(User owner, Pageable pageable);

	Page<Event> findByOwnerAndStatus(User owner, EventStatus status, Pageable pageable);

	Page<Event> findByOwnerAndEventNameContainingIgnoreCase(User owner, String search, Pageable pageable);

	Page<Event> findByOwnerAndStatusAndEventNameContainingIgnoreCase(User owner, EventStatus status, String search,
			Pageable pageable);

	
	// Có early date startdate
	Page<Event> findByStartDateGreaterThanEqual(LocalDate startDate, Pageable pageable);

	Page<Event> findByStatusAndStartDateGreaterThanEqual(EventStatus status, LocalDate startDate, Pageable pageable);

	Page<Event> findByEventNameContainingIgnoreCaseAndStartDateGreaterThanEqual(String search, LocalDate startDate,
			Pageable pageable);

	Page<Event> findByStatusAndEventNameContainingIgnoreCaseAndStartDateGreaterThanEqual(EventStatus status,
			String search, LocalDate startDate, Pageable pageable);

	Page<Event> findByOwnerAndStartDateGreaterThanEqual(User owner, LocalDate startDate, Pageable pageable);

	Page<Event> findByOwnerAndStatusAndStartDateGreaterThanEqual(User owner, EventStatus status, LocalDate startDate,
			Pageable pageable);

	Page<Event> findByOwnerAndEventNameContainingIgnoreCaseAndStartDateGreaterThanEqual(User owner, String search,
			LocalDate startDate, Pageable pageable);

	Page<Event> findByOwnerAndStatusAndEventNameContainingIgnoreCaseAndStartDateGreaterThanEqual(User owner,
			EventStatus status, String search, LocalDate startDate, Pageable pageable);

}
