package touristAgency.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripRequest {
    private String name;
    private String title;

    private String description;

    private Long categoryId;
    private String activityCategory;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
    private String duration;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    @Min(value = 0, message = "Required guides cannot be negative")
    private Integer requiredGuides;
    private Integer requiredGuideCount;

    private String location;
    private String locationName;
    private String departurePoint;

    @Builder.Default
    private List<Long> additionalServiceIds = new ArrayList<>();

    @Builder.Default
    private List<String> additionalServices = new ArrayList<>();

    public String resolveName() {
        if (name != null && !name.isBlank()) return name;
        if (title != null && !title.isBlank()) return title;
        return null;
    }

    public Integer resolveRequiredGuides() {
        if (requiredGuides != null) return requiredGuides;
        if (requiredGuideCount != null) return requiredGuideCount;
        return null;
    }

    public String resolveLocationName() {
        if (locationName != null && !locationName.isBlank()) return locationName;
        if (location != null && !location.isBlank()) return location;
        return null;
    }
}