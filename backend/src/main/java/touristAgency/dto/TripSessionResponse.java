package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSessionResponse {
    private Long id;
    private Long tripId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Long availableSeats;
    private boolean active;
}