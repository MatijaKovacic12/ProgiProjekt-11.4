package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import touristAgency.models.LocationType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Long id;
    private String name;
    private String description;
    private String significance;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocationType type;
}