package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Termin kojemu je vodič dodijeljen (FR-21, UC-16). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideSessionResponse {
    private Long sessionId;
    private Long tripId;
    private String tripName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String departurePoint;
    private Long bookedPeople;
}
