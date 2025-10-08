package me.trihung.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import me.trihung.enums.CheckInStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCheckInStatusRequest {
    
    @NotNull(message = "Status không được để trống")
    private CheckInStatus status;
}