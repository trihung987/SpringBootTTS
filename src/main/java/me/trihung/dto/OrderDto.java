package me.trihung.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private UUID id;
    private UUID zoneId; 
    private UUID ownerId;
    private Integer quantity;
    private BigDecimal totalAmount;   
    private LocalDateTime createdAt;
    private BigDecimal priceZone; 
    private String nameZone;
    private String nameEvent;
    private String urlPayment;

    public OrderDto(UUID id, UUID zoneId, UUID ownerId, Integer quantity, BigDecimal totalAmount,
                    LocalDateTime createdAt, BigDecimal priceZone, String nameZone,
                    String nameEvent, JsonNode paymentData) {
        this.id = id;
        this.zoneId = zoneId;
        this.ownerId = ownerId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.priceZone = priceZone;
        this.nameZone = nameZone;
        this.nameEvent = nameEvent;
        this.urlPayment = extractUrl(paymentData);
    }

    private String extractUrl(JsonNode node) {
        if (node != null && node.has("urlTransaction")) {
            return node.get("urlTransaction").asText();
        }
        return null;
    }
}

