package touristAgency.repositories.projection;

import java.time.OffsetDateTime;

public interface TripSessionAvailability {
    Long getSessionId();
    Long getTripId();
    OffsetDateTime getStartTime();
    OffsetDateTime getEndTime();
    Long getAvailableSeats();
}
