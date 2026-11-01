package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchCriteria {
    private String search;
    private String location;
    private Long categoryId;
    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime dateTo;

    private Integer minDuration;
    private Integer maxDuration;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @Builder.Default
    private Integer requiredSeats = 1;
}