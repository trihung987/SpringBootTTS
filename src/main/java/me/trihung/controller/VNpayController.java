package me.trihung.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.trihung.enums.OrderStatus;
import me.trihung.service.PaymentService;

@RestController
@RequestMapping("/api/v1/paymentvnp")
@Slf4j
@RequiredArgsConstructor
public class VNpayController {
	private final PaymentService paymentService;

	@GetMapping("/ipn")
	public ResponseEntity<OrderStatus> getPaymentIpn(@RequestParam Map<String, String> allParam) {
		log.debug("IPN " + allParam.values().toString());
		return ResponseEntity.ok(paymentService.validPayment(allParam));
	}

	@GetMapping("/validreturn")
	public ResponseEntity<OrderStatus> validReturn(@RequestParam Map<String, String> allParam) {
		return ResponseEntity.ok(paymentService.validUrlReturn(allParam));
	}

	@GetMapping
	public ResponseEntity<String> getPaymentUrl() {
		return ResponseEntity.ok(paymentService.testApi());
	}

}