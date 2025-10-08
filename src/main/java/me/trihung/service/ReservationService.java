package me.trihung.service;

import me.trihung.dto.ReservationDto;
import me.trihung.dto.request.HoldReservationRequest;
import me.trihung.entity.Reservation;

import java.util.UUID;

public interface ReservationService {
 
	ReservationDto holdTickets(HoldReservationRequest holdReservationRequest);

	void confirmReservation(Reservation reservation);
	
	Reservation markAsPendingPayment(Reservation reservation);

	void cancelReservation(UUID reservationId);
	
	ReservationDto getReservationById(UUID reservationId);
	
	UUID getReservationNow();
	
	
}
