package touristAgency.dto;

import touristAgency.models.ReservationStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ReservationDetailsResponse(
        Long id,
        Long tripId,
        Long sessionId,
        String tourTitle,
        OffsetDateTime startTime,
        Integer participants,
        List<Long> addonIds,
        BigDecimal totalPrice,
        ReservationStatus status) {
}