package me.trihung.config;

import lombok.RequiredArgsConstructor;
import me.trihung.enums.OrderStatus;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@RequiredArgsConstructor
@Component
public class Utils {
	
	private final VNPayConfig vnPayConfig;
	
	
	
	public OrderStatus orderReturn(Map<String, String> requestParams) {
		Map<String, String> fields = new HashMap<>();
		
		for (Entry<String, String> entry : requestParams.entrySet()) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }
		String vnp_SecureHash = requestParams.get("vnp_SecureHash");
		if (fields.containsKey("vnp_SecureHashType")) {
			fields.remove("vnp_SecureHashType");
		}
		if (fields.containsKey("vnp_SecureHash")) {
			fields.remove("vnp_SecureHash");
		}
		String signValue = vnPayConfig.hashAllFields(fields);
		if (signValue.equals(vnp_SecureHash)) {
			if ("00".equals(requestParams.get("vnp_TransactionStatus"))) {
				return OrderStatus.COMPLETED;
			} else {
				return OrderStatus.FAILED;
			}
		} else {
			return null;
		}
	}
	
	public String getUrlTransaction(JsonNode node) {
		if (node != null && node.has("urlTransaction")) {
			return node.get("urlTransaction").asText();
		}
		return null;
	}
}
