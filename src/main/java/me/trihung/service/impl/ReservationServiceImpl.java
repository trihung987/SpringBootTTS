package me.trihung.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.trihung.dto.ReservationDto;
import me.trihung.dto.request.HoldReservationRequest;
import me.trihung.entity.Order;
import me.trihung.entity.Reservation;
import me.trihung.entity.User;
import me.trihung.entity.Zone;
import me.trihung.enums.ReservationStatus;
import me.trihung.exception.BadRequestException;
import me.trihung.exception.UnauthorizedException;
import me.trihung.helper.SecurityHelper;
import me.trihung.mapper.ReservationMapper;
import me.trihung.repository.OrderRepository;
import me.trihung.repository.ReservationRepository;
import me.trihung.repository.ZoneRepository;
import me.trihung.service.ReservationService;

@Service
public class ReservationServiceImpl implements ReservationService {
	@Autowired
	private ZoneRepository zoneRepository;
	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private SecurityHelper securityHelper;
	@Autowired
	private OrderRepository orderRepository;


	// Thời gian giữ vé mặc định (10 phút)
	// Chuyển qua thanh toán sẽ được reset về 10p
	private static final int HOLD_MINUTES = 10;
	private static final int PENDING_MINUTES = 10;

	// Giữ vé 15p
	@Transactional
	public ReservationDto holdTickets(HoldReservationRequest holdReservationRequest) {
		UUID zoneId = holdReservationRequest.getZoneId();
		int quantity = holdReservationRequest.getQuantity();
		Zone zone = zoneRepository.findById(zoneId)
				.orElseThrow(() -> BadRequestException.message("Không tìm thấy zone"));

		// Vé khả dụng = maxTickets - soldTickets - reserved
//		int reserved = reservationRepository.countActiveReservations(zoneId);
//		int available = zone.getMaxTickets() - zone.getSoldTickets() - reserved;
//
//		if (available < quantity) {
//			throw BadRequestException.message("Không đủ số lượng vé trong zone này");
//		}
		
		LocalDateTime now = LocalDateTime.now();
		User owner = securityHelper.getCurrentUser();
		Optional<UUID> reservationId = reservationRepository.findFirstIdByOwnerAndExpiresAtAfterOrderByCreatedAtAsc(owner, now);
		
		if (reservationId.isPresent()) {
			throw BadRequestException.message("Đã tồn tại giữ chỗ rồi không thể đặt chỗ thêm");
		}
		
		Reservation reservation = Reservation.builder()
				.zone(zone)
				.quantity(quantity)
				.owner(owner)
				.createdAt(now)
				.expiresAt(LocalDateTime.now().plusMinutes(HOLD_MINUTES))
				.status(ReservationStatus.HOLD)
				.id(UUID.randomUUID())
				.build();
		int result = reservationRepository.tryInsertReservation(reservation.getId(), zone.getId(), owner.getId(), quantity, reservation.getCreatedAt(),
				reservation.getExpiresAt(), reservation.getStatus().name());
		if (result == 0)
			throw BadRequestException.message("Không đủ số lượng vé trong zone này, số vé còn lại đang được giữ chỗ chờ đợi thanh toán. Vui lòng thử lại sau");
		ReservationDto reservationDto = ReservationMapper.INSTANCE.toDto(reservation);
		return reservationDto;
	}
//	 @Param("zoneId") UUID zoneId,
//     @Param("ownerId") UUID ownerId,
//     @Param("quantity") int quantity,
//     @Param("createdAt") LocalDateTime createdAt,
//     @Param("expiresAt") LocalDateTime expiresAt,
//     @Param("status") ReservationStatus status
	@Override
	@Transactional(readOnly = true)
	public ReservationDto getReservationById(UUID reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> BadRequestException.message("Không tìm thấy giữ chỗ"));
	    validateOwner(reservation);
	    return ReservationMapper.INSTANCE.toDto(reservation);
	}


	@Transactional
	public Reservation markAsPendingPayment(Reservation reservation) {
		// Chỉ cho phép chuyển sang trạng thái chờ thanh toán nếu đang HOLD
		if (reservation.getStatus() != ReservationStatus.HOLD) {
			throw BadRequestException.message("Giữ chỗ đang trong trạng thái chờ thanh toán hoặc đã bị hủy");
		}
		 
		// Cập nhật trạng thái + thời gian hết hạn mới
		reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
		reservation.setExpiresAt(LocalDateTime.now().plusMinutes(PENDING_MINUTES));

		// Lưu lại vào DB
		return reservation;
	}

	// xác nhận giữ chỗ
	@Transactional
	public void confirmReservation(Reservation reservation) {
//		Reservation reservation = reservationRepository.findById(reservationId)
//				.orElseThrow(() -> BadRequestException.message("Giữ chỗ không tìm thấy"));

//		if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
//			throw BadRequestException.message("Giữ chỗ đã hết hạn");
//		}
//		
//		if (reservation.getStatus() != ReservationStatus.HOLD) {
//			throw BadRequestException.message("Giữ chỗ này không trong trạng thái thanh toán");
//		}

		Zone zone = reservation.getZone();
		

		// Cộng vào soldTickets đã bán
		zone.setSoldTickets(zone.getSoldTickets() + reservation.getQuantity());
		zoneRepository.save(zone);
		
		
		reservationRepository.delete(reservation);
		
	}
	
	@Transactional
	public void cancelReservation(UUID reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> BadRequestException.message("Giữ chỗ không tìm thấy"));
		Order order = reservation.getOrder();
		if (order != null) {
			orderRepository.delete(order);
		}
		reservationRepository.deleteById(reservationId);
	}
	
	public void validateOwner(Reservation reservation) {
		User user = securityHelper.getCurrentUser();
		if (!user.equals(reservation.getOwner()))
			throw UnauthorizedException.message("Bạn không phải chủ sở hữu của đơn giữ chỗ này");
	}
	@Override 
	public UUID getReservationNow() {
		LocalDateTime now = LocalDateTime.now();
		User owner = securityHelper.getCurrentUser();
		UUID reservationId = reservationRepository.findFirstIdByOwnerAndExpiresAtAfterOrderByCreatedAtAsc(owner, now)
				.orElseThrow(()-> BadRequestException.message("Không tìm thấy giữ chỗ nào"));
		
		return reservationId;
	}
}
