package me.trihung.controller;

import me.trihung.dto.OrderDto;
import me.trihung.dto.OrderPageResponse;
import me.trihung.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {

    @Autowired
    private OrderService orderService; 

    @GetMapping
    public ResponseEntity<OrderPageResponse> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return ResponseEntity.ok(orderService.getOrdersPaged(page, size, sortBy, sortDirection));
    }
    
    @PostMapping("/{id}")
    public ResponseEntity<OrderDto> createOrderFromReservaationId(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(orderService.createOrderFromReservation(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<OrderPageResponse> getOrdersByEvent(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        return ResponseEntity.ok(
                orderService.getOrdersByEvent(eventId, zoneId, page, size, sortBy, sortDirection)
        );
    }
}
