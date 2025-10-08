package me.trihung.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import me.trihung.config.Utils;
import me.trihung.dto.OrderDto;
import me.trihung.dto.OrderPageResponse;
import me.trihung.entity.Event;
import me.trihung.entity.Order;
import me.trihung.entity.Payment;
import me.trihung.entity.Reservation;
import me.trihung.entity.User;
import me.trihung.entity.Zone;
import me.trihung.enums.OrderStatus;
import me.trihung.exception.BadRequestException;
import me.trihung.exception.UnauthorizedException;
import me.trihung.helper.SecurityHelper;
import me.trihung.mapper.OrderMapper;
import me.trihung.repository.EventRepository;
import me.trihung.repository.OrderRepository;
import me.trihung.repository.ReservationRepository;
import me.trihung.repository.ZoneRepository;
import me.trihung.service.OrderService;
import me.trihung.service.PaymentService;
import me.trihung.service.ReservationService;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private ZoneRepository zoneRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SecurityHelper securityHelper;
    
    @Autowired
    private Utils utils;
    
    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDto createOrderFromReservation(UUID reservationId, HttpServletRequest request) {
    	Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> BadRequestException.message("Giữ chỗ không tìm thấy"));

		if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw BadRequestException.message("Giữ chỗ đã hết hạn");
		}
		
        BigDecimal total = reservation.getZone().getPrice()
                .multiply(BigDecimal.valueOf(reservation.getQuantity()));
        validateOwner(reservation);
        Payment payment = null; //cap nhap payment service
        Order order = null;
        if (reservation.getOrder()==null) {
        	 order = Order.builder()
                     .zone(reservation.getZone())
                     .owner(reservation.getOwner())
                     .quantity(reservation.getQuantity())
                     .totalAmount(total)
                     .payment(payment)
                     .createdAt(LocalDateTime.now())
                     .orderStatus(OrderStatus.PENDING)
                     
                     .build();
        	 order = orderRepository.save(order);
        	 payment = paymentService.createPayment(order, request);
        	 order.setPayment(payment);
        	 reservation.setOrder(order);
        	 reservation = reservationService.markAsPendingPayment(reservation);
             reservationRepository.save(reservation);

        	 
        }else {
        	order = reservation.getOrder();
        	payment = order.getPayment();
        }
         
        String urlPayment = utils.getUrlTransaction(payment.getPaymentData());
        System.out.println("URLPAYMENT="+urlPayment);
        OrderDto orderDto = orderMapper.toDto(order, urlPayment);
        
        return orderDto;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse getOrdersPaged(
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            sort = Sort.by(
                    "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC,
                    sortBy
            );
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        User user = securityHelper.getCurrentUser();
        Page<OrderDto> orderPage = orderRepository.findOrderDtosByOwner(user, pageable);

        return new OrderPageResponse(
                orderPage.getContent(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.getNumber(),
                orderPage.getSize()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy order với id: " + id));
        String urlPayment = utils.getUrlTransaction(order.getPayment().getPaymentData());
        return orderMapper.toDto(order, urlPayment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse getOrdersByEvent(
            UUID eventId,
            UUID zoneId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        // Kiểm tra event có tồn tại không
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy event với id: " + eventId));

        // Validate owner - chỉ owner của event mới xem được orders
        validateEventOwner(event);

        // Tạo sort
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDto> orderPage;

        // Nếu có zoneId thì filter theo zone, không thì lấy tất cả
        if (zoneId != null) {
            // Kiểm tra zone có tồn tại không
            Zone zone = zoneRepository.findById(zoneId)
                    .orElseThrow(() -> BadRequestException.message("Không tìm thấy zone với id: " + zoneId));

            // Kiểm tra zone có thuộc event không
            if (!zone.getEvent().getId().equals(eventId)) {
                throw BadRequestException.message("Zone không thuộc event này");
            }

            // Lấy orders theo event và zone (chỉ COMPLETED)
            orderPage = orderRepository.findOrderDtosByEventIdAndZoneIdAndStatus(
                    eventId,
                    zoneId,
                    OrderStatus.COMPLETED,
                    pageable
            );
        } else {
            // Lấy orders theo event (chỉ COMPLETED)
            orderPage = orderRepository.findOrderDtosByEventIdAndStatus(
                    eventId,
                    OrderStatus.COMPLETED,
                    pageable
            );
        }

        return new OrderPageResponse(
                orderPage.getContent(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.getNumber(),
                orderPage.getSize()
        );
    }

    /**
     * Tạo Sort object từ sortBy và sortDirection
     */
    private Sort createSort(String sortBy, String sortDirection) {
        // Default sort: createdAt DESC (mới nhất)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;

            // Validate sortBy field
            switch (sortBy.toLowerCase()) {
                case "createdat":
                    sort = Sort.by(direction, "createdAt");
                    break;
                case "totalamount":
                    sort = Sort.by(direction, "totalAmount");
                    break;
                case "quantity":
                    sort = Sort.by(direction, "quantity");
                    break;
                default:
                    // Default to createdAt if invalid field
                    sort = Sort.by(direction, "createdAt");
                    break;
            }
        }

        return sort;
    }

   
    private void validateEventOwner(Event event) {
        User user = securityHelper.getCurrentUser();
        if (!user.equals(event.getOwner())) {
            throw UnauthorizedException.message("Bạn không phải chủ sở hữu sự kiện này");
        }
    }
    
    public void validateOwner(Reservation reservation) {
		User user = securityHelper.getCurrentUser();
		if (!user.equals(reservation.getOwner()))
			throw UnauthorizedException.message("Bạn không phải chủ sở hữu sự kiện này");
	}
    
    
}
