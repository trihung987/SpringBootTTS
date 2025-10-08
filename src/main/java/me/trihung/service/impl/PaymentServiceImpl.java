package me.trihung.service.impl;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.trihung.config.Utils;
import me.trihung.config.VNPayConfig;
import me.trihung.entity.Order;
import me.trihung.entity.Payment;
import me.trihung.entity.Reservation;
import me.trihung.entity.User;
import me.trihung.enums.OrderStatus;
import me.trihung.exception.BadRequestException;
import me.trihung.helper.SecurityHelper;
import me.trihung.repository.OrderRepository;
import me.trihung.repository.PaymentRepository;
import me.trihung.repository.ReservationRepository;
import me.trihung.service.CheckInService;
import me.trihung.service.PaymentService;
import me.trihung.service.ReservationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	
	private final ReservationRepository reservationRepository;
	
	private final PaymentRepository paymentRepository;
	
	private final CheckInService checkInService;

	private final ObjectMapper objectMapper;

	private final SecurityHelper securityHelper;

	private final ReservationService reservationService;
	
	private final VNPayConfig vnPayConfig;
	
	private final Utils utils;

	@Override 
	public Payment createPayment(Order order, HttpServletRequest request) {		
		String orderId = order.getId().toString();
		String url = vnPayConfig.createPaymentRedirect(order.getTotalAmount().longValue(),
				"Đơn thanh toán cho mã đơn: " + orderId, order.getId().toString(), request);
		ObjectNode data = objectMapper.createObjectNode();
		data.put("urlTransaction", url);
		data.put("vnp_ref", orderId);
		Payment payment = Payment.builder().amount(order.getTotalAmount())
				.paymentMethod("VNPAY")
				.order(order)
				.paymentData(data)
				.transactionId(orderId)
				.build();
		return payment;
	}


	@Override
	@Transactional
	public OrderStatus validPayment(Map<String, String> requestParams) {
		Optional<Payment> payment = null;

		if (requestParams.containsKey("vnp_TxnRef")) {
			String vnp_ref = requestParams.get("vnp_TxnRef");

			payment = paymentRepository.findByTransactionId(vnp_ref);
			if (payment.isPresent()) {

			} else
				return OrderStatus.FAILED;
		}
		Payment pay = payment.get();

		OrderStatus result = utils.orderReturn(requestParams);
		String transactionId = requestParams.get("vnp_TransactionNo");
		pay.setTransactionId(result == OrderStatus.COMPLETED ? transactionId : pay.getTransactionId());
		if (pay.getPaymentData() != null && pay.getPaymentData() instanceof ObjectNode) {
			ObjectNode objectNode = (ObjectNode) pay.getPaymentData();
			requestParams.forEach(objectNode::put);
			pay.setPaymentData(objectNode);
		}
		Order order = pay.getOrder();
		order.setOrderStatus(result);
		
		Reservation reservation = reservationRepository.findByOrder(order)
				.orElseThrow(() -> BadRequestException.message("Giữ chỗ không tìm thấy"));
		if (result==OrderStatus.FAILED)
			reservationService.cancelReservation(reservation.getId());
		else 
			reservationService.confirmReservation(reservation);
		
		
		paymentRepository.save(pay);
		
		checkInService.createCheckInsFromOrder(order.getId());
		
		log.debug("update payment {} with status {} ", pay.getId(), result);

		return result; 
	}

	@Override
	public String testApi() {
		return vnPayConfig.createPaymentRedirect(Long.parseLong("10000"), "test order", "abcdeg12345678", null);
	}

	@Override
	public OrderStatus validUrlReturn(Map<String, String> requestParams) {
		return utils.orderReturn(requestParams);
	}

}
