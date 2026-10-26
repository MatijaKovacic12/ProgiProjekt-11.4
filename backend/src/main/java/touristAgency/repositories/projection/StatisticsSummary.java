package touristAgency.repositories.projection;

import java.math.BigDecimal;

public interface StatisticsSummary {
    Long getReservationCount();
    Long getCancelledCount();
    BigDecimal getRevenue();
}
