package touristAgency.dto;

import touristAgency.models.ReservationStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record MyReservationResponse(
        Long id,
        Long tripId,
        Long sessionId,
        String tourTitle,
        OffsetDateTime startTime,
        String departurePoint,
        Integer participants,
        List<String> addons,
        BigDecimal totalPrice,
        ReservationStatus status) {
}