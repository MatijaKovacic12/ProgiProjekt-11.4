package touristAgency.dto;

import touristAgency.models.ReservationStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminReservationResponse(
        Long id,
        Long tripId,
        String tourTitle,
        Long sessionId,
        OffsetDateTime startTime,
        String userName,
        String userEmail,
        Integer participants,
        List<String> addons,
        ReservationStatus status,
        OffsetDateTime createdAt,
        BigDecimal totalPrice) {
}