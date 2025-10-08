package me.trihung.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCheckInRequest {
    
    @NotNull(message = "Order ID không được để trống")
    private UUID orderId;
}