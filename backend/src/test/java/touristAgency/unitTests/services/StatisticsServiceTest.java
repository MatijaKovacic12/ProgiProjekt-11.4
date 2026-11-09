package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import touristAgency.dto.StatisticsResponse;
import touristAgency.exceptions.BadRequestException;
import touristAgency.repositories.ReservationAdditionalServiceRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;
import touristAgency.services.StatisticsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 11, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 30);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationAdditionalServiceRepository additionalServiceRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getStatistics_returnsDataFromRepositories() {
        stubRepositories(
                summary(42L, 3L, "4890.00"),
                List.of(occupancy(5L, "Rafting na Cetini", 4L, 48L, 41L, 85.4)),
                List.of(topItem(1L, "Rafting", 15L)),
                List.of(topItem(7L, "Prijevoz", 30L)));

        StatisticsResponse result = statisticsService.getStatistics(FROM, TO);

        assertEquals(FROM, result.from());
        assertEquals(TO, result.to());
        assertEquals(42L, result.reservationCount());
        assertEquals(3L, result.cancelledCount());
        assertEquals(new BigDecimal("4890.00"), result.revenue());
        assertEquals(1, result.tripOccupancy().size());
        assertEquals("Rafting na Cetini", result.tripOccupancy().get(0).name());
        assertEquals(85.4, result.tripOccupancy().get(0).occupancyPercent());
        assertEquals("Rafting", result.topCategories().get(0).name());
        assertEquals(30L, result.topAdditionalServices().get(0).total());
    }

    @Test
    void getStatistics_returnsZerosAndEmptyListsWhenNoData() {
        stubRepositories(summary(0L, 0L, "0"), List.of(), List.of(), List.of());

        StatisticsResponse result = statisticsService.getStatistics(FROM, TO);

        assertEquals(0L, result.reservationCount());
        assertEquals(0L, result.cancelledCount());
        assertEquals(0, result.revenue().compareTo(BigDecimal.ZERO));
        assertTrue(result.tripOccupancy().isEmpty());
        assertTrue(result.topCategories().isEmpty());
        assertTrue(result.topAdditionalServices().isEmpty());
    }

    @Test
    void getStatistics_includesWholeLastDay() {
        stubRepositories(summary(0L, 0L, "0"), List.of(), List.of(), List.of());

        statisticsService.getStatistics(FROM, TO);

        OffsetDateTime expectedStart = OffsetDateTime.of(2026, 11, 1, 0, 0, 0, 0, ZoneOffset.ofHours(1));
        OffsetDateTime expectedEnd = OffsetDateTime.of(2026, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(1));
        verify(reservationRepository).getStatisticsSummary(expectedStart, expectedEnd);
        verify(additionalServiceRepository).findTop5AdditionalServices(expectedStart, expectedEnd);
    }

    @Test
    void getStatistics_rejectsEndBeforeStart() {
        assertThrows(BadRequestException.class, () -> statisticsService.getStatistics(TO, FROM));

        verifyNoInteractions(reservationRepository, additionalServiceRepository);
    }

    @Test
    void getStatistics_rejectsMissingDate() {
        assertThrows(BadRequestException.class, () -> statisticsService.getStatistics(null, TO));

        verifyNoInteractions(reservationRepository, additionalServiceRepository);
    }

    @Test
    void exportCsv_containsSameValuesAsStatistics() {
        stubRepositories(
                summary(42L, 3L, "4890.00"),
                List.of(occupancy(5L, "Rafting na Cetini", 4L, 48L, 41L, 85.4)),
                List.of(topItem(1L, "Rafting", 15L)),
                List.of(topItem(7L, "Prijevoz", 30L)));

        String csv = statisticsService.exportCsv(FROM, TO);

        assertTrue(csv.contains("2026-11-01;2026-11-30\n"));
        assertTrue(csv.contains("42;3;4890.00\n"));
        assertTrue(csv.contains("Rafting na Cetini;4;48;41;85.4\n"));
        assertTrue(csv.contains("Rafting;15\n"));
        assertTrue(csv.contains("Prijevoz;30\n"));
    }

    @Test
    void exportCsv_putsValueWithSemicolonInQuotes() {
        stubRepositories(
                summary(1L, 0L, "55.00"),
                List.of(occupancy(5L, "Rafting; Cetina", 1L, 12L, 3L, 25.0)),
                List.of(),
                List.of());

        String csv = statisticsService.exportCsv(FROM, TO);

        assertTrue(csv.contains("\"Rafting; Cetina\";1;12;3;25.0\n"));
    }

    // ---- pomoćne metode: lažni odgovori baze ----

    /** Lažni repozitoriji na svako pitanje vrate zadane odgovore. */
    private void stubRepositories(StatisticsSummary summary, List<TripOccupancy> occupancy,
                                  List<TopItem> categories, List<TopItem> services) {
        when(reservationRepository.getStatisticsSummary(any(), any())).thenReturn(summary);
        when(reservationRepository.getTripOccupancy(any(), any())).thenReturn(occupancy);
        when(reservationRepository.findTop5Categories(any(), any())).thenReturn(categories);
        when(additionalServiceRepository.findTop5AdditionalServices(any(), any())).thenReturn(services);
    }

    private StatisticsSummary summary(Long count, Long cancelled, String revenue) {
        StatisticsSummary s = mock(StatisticsSummary.class);
        when(s.getReservationCount()).thenReturn(count);
        when(s.getCancelledCount()).thenReturn(cancelled);
        when(s.getRevenue()).thenReturn(new BigDecimal(revenue));
        return s;
    }

    private TripOccupancy occupancy(Long tripId, String name, Long sessions, Long capacity,
                                    Long booked, Double percent) {
        TripOccupancy o = mock(TripOccupancy.class);
        when(o.getTripId()).thenReturn(tripId);
        when(o.getName()).thenReturn(name);
        when(o.getSessionCount()).thenReturn(sessions);
        when(o.getCapacity()).thenReturn(capacity);
        when(o.getBookedPeople()).thenReturn(booked);
        when(o.getOccupancyPercent()).thenReturn(percent);
        return o;
    }

    private TopItem topItem(Long id, String name, Long total) {
        TopItem t = mock(TopItem.class);
        when(t.getId()).thenReturn(id);
        when(t.getName()).thenReturn(name);
        when(t.getTotal()).thenReturn(total);
        return t;
    }
}