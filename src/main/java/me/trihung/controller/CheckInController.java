package me.trihung.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.trihung.dto.CheckInDTO;
import me.trihung.dto.request.CreateCheckInRequest;
import me.trihung.dto.request.UpdateCheckInStatusRequest;
import me.trihung.enums.CheckInStatus;
import me.trihung.service.CheckInService;

@RestController
@RequestMapping("/api/v1/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

//    @PostMapping
//    public ResponseEntity<List<CheckInDTO>> createCheckInsFromOrder(
//            @Valid @RequestBody CreateCheckInRequest request) {
//        List<CheckInDTO> checkIns = checkInService.createCheckInsFromOrder(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(checkIns);
//    }

    /**
     * Lấy danh sách check-in theo event (có phân trang)
     * GET /api/check-ins/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<Page<CheckInDTO>> getCheckInsByEvent(
            @PathVariable UUID eventId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CheckInDTO> checkIns = checkInService.getCheckInsByEvent(eventId, pageable);
        return ResponseEntity.ok(checkIns);
    }

    /**
     * Lấy danh sách check-in theo event và status (có phân trang)
     * GET /api/check-ins/event/{eventId}/status/{status}
     */
    @GetMapping("/event/{eventId}/status/{status}")
    public ResponseEntity<Page<CheckInDTO>> getCheckInsByEventAndStatus(
            @PathVariable UUID eventId,
            @PathVariable CheckInStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CheckInDTO> checkIns = checkInService.getCheckInsByEventAndStatus(eventId, status, pageable);
        return ResponseEntity.ok(checkIns);
    }

    /**
     * Lấy danh sách check-in theo zone (có phân trang)
     * GET /api/check-ins/zone/{zoneId}
     */
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<Page<CheckInDTO>> getCheckInsByZone(
            @PathVariable UUID zoneId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CheckInDTO> checkIns = checkInService.getCheckInsByZone(zoneId, pageable);
        return ResponseEntity.ok(checkIns);
    }

    /**
     * Cập nhật status của check-in
     * PATCH /api/check-ins/{checkInId}/status
     */
    @PostMapping("/{checkInId}/status")
    public ResponseEntity<CheckInDTO> updateCheckInStatus(
            @PathVariable UUID checkInId,
            @Valid @RequestBody UpdateCheckInStatusRequest request) {
        CheckInDTO updatedCheckIn = checkInService.updateCheckInStatus(checkInId, request);
        return ResponseEntity.ok(updatedCheckIn);
    }

    /**
     * Lấy chi tiết check-in theo ID
     * GET /api/check-ins/{checkInId}
     */
    @GetMapping("/{checkInId}")
    public ResponseEntity<CheckInDTO> getCheckInById(@PathVariable UUID checkInId) {
        CheckInDTO checkIn = checkInService.getCheckInById(checkInId);
        return ResponseEntity.ok(checkIn);
    }
    
    @GetMapping("/code/{checkInCode}")
    public ResponseEntity<CheckInDTO> getCheckInByCode(@PathVariable String checkInCode) {
        CheckInDTO checkIn = checkInService.getCheckInByCode(checkInCode);
        return ResponseEntity.ok(checkIn);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<CheckInDTO>> getCheckInsByOrderId(@PathVariable UUID orderId) {
        List<CheckInDTO> checkIns = checkInService.getCheckInsByOrderId(orderId);
        return ResponseEntity.ok(checkIns);
    }
}