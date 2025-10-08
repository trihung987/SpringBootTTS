package me.trihung.dto;

import lombok.*;
import me.trihung.enums.CheckInStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInDTO {
    private UUID id;
    private String checkInCode;
    private UUID orderId;
    private UUID zoneId;
    private String zoneName;
    private UUID buyerId;
    private String buyerName;
    private String buyerEmail;
    private CheckInStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}