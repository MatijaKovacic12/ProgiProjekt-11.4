package touristAgency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatisticsResponse (
        LocalDate from,
        LocalDate to,
        Long reservationCount,
        Long cancelledCount,
        BigDecimal revenue,
        List<TripOccupancyResponse> tripOccupancy,
        List<TopItemResponse> topCategories,
        List<TopItemResponse> topAdditionalServices) {

}