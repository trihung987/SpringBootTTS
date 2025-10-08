package me.trihung.service.impl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.trihung.dto.CheckInDTO;
import me.trihung.dto.request.UpdateCheckInStatusRequest;
import me.trihung.entity.CheckIn;
import me.trihung.entity.Event;
import me.trihung.entity.Order;
import me.trihung.entity.User;
import me.trihung.entity.Zone;
import me.trihung.enums.CheckInStatus;
import me.trihung.enums.OrderStatus;
import me.trihung.exception.BadRequestException;
import me.trihung.exception.UnauthorizedException;
import me.trihung.helper.SecurityHelper;
import me.trihung.mapper.CheckInMapper;
import me.trihung.repository.CheckInRepository;
import me.trihung.repository.EventRepository;
import me.trihung.repository.OrderRepository;
import me.trihung.repository.ZoneRepository;
import me.trihung.service.CheckInService;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final OrderRepository orderRepository;
    private final ZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final CheckInMapper checkInMapper;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public List<CheckInDTO> createCheckInsFromOrder(UUID orderid) {
        // Tìm order
        Order order = orderRepository.findById(orderid)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy order với ID: " + orderid));

        // Kiểm tra order đã COMPLETED chưa
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw BadRequestException.message("Order chưa được hoàn thành. Không thể tạo check-in.");
        }

        // Kiểm tra đã tạo check-in cho order này chưa
        List<CheckIn> existingCheckIns = checkInRepository.findByOrderId(order.getId());
        if (!existingCheckIns.isEmpty()) {
            throw BadRequestException.message("Check-in cho order này đã được tạo trước đó.");
        }

        List<CheckIn> checkIns = new ArrayList<>();
        Integer quantity = order.getQuantity();

        // Tạo số lượng check-in theo quantity
        for (int i = 0; i < quantity; i++) {
            String checkInCode = generateCheckInCode();
            
            CheckIn checkIn = CheckIn.builder()
                    .checkInCode(checkInCode)
                    .order(order)
                    .zone(order.getZone())
                    .buyer(order.getOwner())
                    .status(CheckInStatus.NOT_CHECKED_IN)
                    .checkInTime(null)
                    .build();

            checkIns.add(checkIn);
        }

        // Lưu tất cả check-in
        List<CheckIn> savedCheckIns = checkInRepository.saveAll(checkIns);

        return checkInMapper.toDTOList(savedCheckIns);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CheckInDTO> getCheckInsByEvent(UUID eventId, Pageable pageable) {
    	Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy event với ID: " + eventId));
    	validateOwner(event);
        Page<CheckIn> checkIns = checkInRepository.findAllByEventId(eventId, pageable);
        return checkIns.map(checkInMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CheckInDTO> getCheckInsByEventAndStatus(UUID eventId, CheckInStatus status, Pageable pageable) {
    	Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy event với ID: " + eventId));
    	validateOwner(event);
    	Page<CheckIn> checkIns = checkInRepository.findAllByEventIdAndStatus(eventId, status, pageable);
        return checkIns.map(checkInMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CheckInDTO> getCheckInsByZone(UUID zoneId, Pageable pageable) {
    	Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy zone với ID: " + zoneId));
    	validateOwner(zone.getEvent().getOwner());
        Page<CheckIn> checkIns = checkInRepository.findAllByZoneId(zoneId, pageable);
        return checkIns.map(checkInMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CheckInDTO> getCheckInsByOrderId(UUID orderId) {
    	Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy order với ID: " + orderId));
    	validateOwner(order.getOwner());
    	List<CheckIn> checkIns = checkInRepository.findByOrderId(orderId);
        return checkIns.stream()
                .map(checkInMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CheckInDTO updateCheckInStatus(UUID checkInId, UpdateCheckInStatusRequest request) {
        // Tìm check-in
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy check-in với ID: " + checkInId));

        // Lấy event từ zone
        Event event = checkIn.getZone().getEvent();

        // Validate owner - chỉ owner của event mới có quyền update status
        validateOwner(event);

        // Cập nhật status
        checkIn.setStatus(request.getStatus());

        // Nếu status là CHECKED_IN, cập nhật thời gian check-in
        if (request.getStatus() == CheckInStatus.CHECKED_IN) {
            if (checkIn.getCheckInTime() == null) {
                checkIn.setCheckInTime(LocalDateTime.now());
            }
        } else if (request.getStatus() == CheckInStatus.NOT_CHECKED_IN) {
            // Nếu chuyển về NOT_CHECKED_IN, xóa thời gian check-in
            checkIn.setCheckInTime(null);
        }

        CheckIn updatedCheckIn = checkInRepository.save(checkIn);

        return checkInMapper.toDTO(updatedCheckIn);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInDTO getCheckInById(UUID checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy check-in với ID: " + checkInId));

        return checkInMapper.toDTO(checkIn);
    }
    
    @Override
    @Transactional(readOnly = true) 
    public CheckInDTO getCheckInByCode(String checkInCode) {
        CheckIn checkIn = checkInRepository.findByCheckInCode(checkInCode)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy check-in với mã: " + checkInCode));
        System.out.println("checkin status "+checkInCode+" -> "+checkIn.getStatus().toString());
        if (checkIn.getStatus()==CheckInStatus.CHECKED_IN)
        	throw BadRequestException.message("Vé đã check-in rồi!");
        Event event = checkIn.getZone().getEvent(); 
        validateOwner(event);
        return checkInMapper.toDTO(checkIn);
    }

    /**
     * Validate owner của event
     */
    private void validateOwner(Event event) {
        User user = securityHelper.getCurrentUser();
        if (!user.equals(event.getOwner())) {
            throw UnauthorizedException.message("Bạn không phải chủ sở hữu sự kiện này");
        }
    }
    
    private void validateOwner(User owner) {
        User user = securityHelper.getCurrentUser();
        if (!user.equals(owner)) {
            throw UnauthorizedException.message("Bạn không phải chủ sở hữu sự kiện này");
        }
    }

    /**
     * Generate mã check-in duy nhất (CI001, CI002, ...)
     */
    private String generateCheckInCode() {
        String code;
        do {
            // Tạo mã ngẫu nhiên
            long timestamp = System.currentTimeMillis();
            int random = (int) (Math.random() * 10000);
            code = String.format("CI%d%04d", timestamp % 100000, random);
        } while (checkInRepository.findByCheckInCode(code).isPresent());
        
        return code;
    }
}