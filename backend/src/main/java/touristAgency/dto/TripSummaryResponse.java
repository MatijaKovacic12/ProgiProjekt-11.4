package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryResponse {
    private Long id;
    private String name;
    private String title; // Duplirano s name radi kompatibilnosti s frontendom (tour.title)
    private String description;
    private Long categoryId;
    private String categoryName;
    private String activityCategory; // Frontend koristi tour.activityCategory za badge
    private Integer durationMinutes;
    private String duration; // Formatirano za prikaz: npr. "2.5 hours"
    private BigDecimal price;
    private Integer maxParticipants;
    private Integer requiredGuides;
    private Integer requiredGuideCount;
    private String location; // Naziv glavne lokacije (npr. "Zagreb, Croatia")
    private String departurePoint; // Mjesto polaska (npr. "Ban Jelačić Square")
    private boolean active;
    private List<String> additionalServices;
    private List<Long> additionalServiceIds;
    private List<String> scheduledTimeSlots;
}