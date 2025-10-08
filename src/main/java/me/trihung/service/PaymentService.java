package me.trihung.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import me.trihung.entity.Order;
import me.trihung.entity.Payment;
import me.trihung.enums.OrderStatus;

public interface PaymentService {
	
	Payment createPayment(Order order, HttpServletRequest request);
	
	OrderStatus validPayment(Map<String, String> requestParams);
	
	OrderStatus validUrlReturn(Map<String, String> requestParams);
	
	String testApi();
}
