package me.trihung.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.trihung.dto.CheckInDTO;
import me.trihung.dto.request.UpdateCheckInStatusRequest;
import me.trihung.enums.CheckInStatus;

public interface CheckInService {

    /**
     * Tạo check-in từ order
     * @param request CreateCheckInRequest chứa orderId
     * @return List CheckInDTO đã được tạo
     */
    List<CheckInDTO> createCheckInsFromOrder(UUID orderId);

    /**
     * Lấy danh sách check-in theo event (có phân trang)
     * @param eventId ID của event
     * @param pageable Thông tin phân trang
     * @return Page CheckInDTO
     */
    Page<CheckInDTO> getCheckInsByEvent(UUID eventId, Pageable pageable);

    /**
     * Lấy danh sách check-in theo event và status (có phân trang)
     * @param eventId ID của event
     * @param status CheckInStatus
     * @param pageable Thông tin phân trang
     * @return Page CheckInDTO
     */
    Page<CheckInDTO> getCheckInsByEventAndStatus(UUID eventId, CheckInStatus status, Pageable pageable);

    /**
     * Lấy danh sách check-in theo zone (có phân trang)
     * @param zoneId ID của zone
     * @param pageable Thông tin phân trang
     * @return Page CheckInDTO
     */
    Page<CheckInDTO> getCheckInsByZone(UUID zoneId, Pageable pageable);

    /**
     * Cập nhật status của check-in
     * @param checkInId ID của check-in
     * @param request UpdateCheckInStatusRequest
     * @return CheckInDTO đã được cập nhật 
     */
    CheckInDTO updateCheckInStatus(UUID checkInId, UpdateCheckInStatusRequest request);

    /**
     * Lấy chi tiết check-in theo ID
     * @param checkInId ID của check-in
     * @return CheckInDTO
     */
    CheckInDTO getCheckInById(UUID checkInId);
    
    CheckInDTO getCheckInByCode(String checkInCode);
    
    List<CheckInDTO> getCheckInsByOrderId(UUID orderId);
}