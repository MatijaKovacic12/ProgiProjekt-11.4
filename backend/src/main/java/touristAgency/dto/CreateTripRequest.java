package touristAgency.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {
    private String name;
    private String title;

    private String description;

    private Long categoryId;
    private String activityCategory;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
    private String duration;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    @Min(value = 0, message = "Required guides cannot be negative")
    private Integer requiredGuides;
    private Integer requiredGuideCount;

    private String location;
    private String locationName;
    private String departurePoint;
    private String poiCoordinates;

    @Builder.Default
    private List<Long> additionalServiceIds = new ArrayList<>();

    @Builder.Default
    private List<String> additionalServices = new ArrayList<>();

    @Builder.Default
    private List<OffsetDateTime> scheduledTimeSlots = new ArrayList<>();

    @Builder.Default
    private List<LandmarkRequest> landmarks = new ArrayList<>();

    // Pomoćne metode koje ujednačavaju polja (jer frontend ponekad pošalje 'title', a ponekad 'name')
    public String resolveName() {
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        return null;
    }

    public Integer resolveRequiredGuides() {
        if (requiredGuides != null) return requiredGuides;
        if (requiredGuideCount != null) return requiredGuideCount;
        return 1;
    }

    public String resolveLocationName() {
        if (locationName != null && !locationName.isBlank()) return locationName;
        if (location != null && !location.isBlank()) return location;
        return null;
    }
}