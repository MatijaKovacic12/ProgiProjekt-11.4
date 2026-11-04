package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailsResponse {
    private Long id;
    private String name;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String activityCategory;
    private Integer durationMinutes;
    private String duration;
    private BigDecimal price;
    private Integer maxParticipants;
    private Integer requiredGuides;
    private Integer requiredGuideCount;
    private String location;
    private String departurePoint;
    private boolean active;

    private List<AdditionalServiceResponse> additionalServices;
    private List<TripSessionResponse> scheduledSessions;
    private List<String> scheduledTimeSlots;
    private List<LocationResponse> landmarks;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}