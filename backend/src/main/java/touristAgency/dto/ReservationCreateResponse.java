package touristAgency.dto;

import touristAgency.models.ReservationStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ReservationCreateResponse(
        Long id,
        Long sessionId,
        String tourTitle,
        OffsetDateTime startTime,
        Integer participants,
        List<AddonResponse> addons,
        BigDecimal totalPrice,
        ReservationStatus status) {

    public record AddonResponse(Long id, String name) {
    }
}