package me.trihung.service;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import me.trihung.dto.OrderDto;
import me.trihung.dto.OrderPageResponse;
import me.trihung.entity.Reservation;

public interface OrderService {
    OrderDto createOrderFromReservation(UUID reservationId, HttpServletRequest request);
    OrderPageResponse getOrdersPaged(
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
    OrderDto getOrderById(UUID id);
    OrderPageResponse getOrdersByEvent(
            UUID eventId,
            UUID zoneId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}
