package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationRepositoryTest extends  RepositoryTestBase {
    private static final PageRequest PAGE = PageRequest.of(0, 20);

    private OffsetDateTime from() { return now.minusDays(10); }
    private OffsetDateTime to()   { return now.plusDays(10); }

    @Test
    void save_setsDefaultStatusConfirmed() {
        Reservation r = new Reservation();
        r.setCustomer(customer);
        r.setTripSession(upcoming);
        r.setNumberOfPeople(1);
        r.setTotalPrice(new BigDecimal("50.00"));
        assertEquals(ReservationStatus.CONFIRMED, reservations.saveAndFlush(r).getStatus());
    }

    @Test
    void sumBookedSeats_ignoresCancelledReservations() {
        assertEquals(7, reservations.sumBookedSeats(upcoming.getId()));
        assertEquals(1, reservations.sumBookedSeats(inProgress.getId()));
    }

    @Test
    void sumBookedSeats_returnsZeroForSessionWithoutReservations() {
        assertEquals(0, reservations.sumBookedSeats(session(trip, now.plusDays(20)).getId()));
    }

    @Test
    void findByCustomerId_returnsAllReservationsOfCustomer() {
        List<Reservation> mine = reservations.findByCustomerIdOrderByCreatedAtDesc(customer.getId());

        assertEquals(4, mine.size());
        assertTrue(reservations.findByCustomerIdOrderByCreatedAtDesc(admin.getId()).isEmpty());
    }

    @Test
    void findDetailsById_loadsTripSessionAndServices() {
        Reservation r = reservations.findDetailsById(upcomingRes.getId()).orElseThrow();

        assertEquals("Plitvice", r.getTripSession().getTrip().getName());
        assertEquals(1, r.getReservationAdditionalServices().size());
        assertEquals("Prijevoz", r.getReservationAdditionalServices().get(0).getAdditionalService().getName());
    }


    @Test
    void searchAdmin_searchesNameAndEmailCaseInsensitive() {
        assertEquals(4, reservations.searchAdmin(null, null, null, "ANA", PAGE).getTotalElements());
        assertEquals(4, reservations.searchAdmin(null, null, null, "horvat", PAGE).getTotalElements());
        assertEquals(4, reservations.searchAdmin(null, null, null, "@test.hr", PAGE).getTotalElements());
        assertEquals(0, reservations.searchAdmin(null, null, null, "nepostoji", PAGE).getTotalElements());
    }


    @Test
    void findActiveInProgress_returnsOnlyConfirmedReservationOfRunningSession() {
        List<Reservation> active = reservations.findActiveInProgress(customer.getId(), now);

        assertEquals(1, active.size());
        assertEquals(inProgressRes.getId(), active.get(0).getId());
    }

    @Test
    void markFinished_completesOnlyConfirmedReservationsOfEndedSessions() {
        assertEquals(1, reservations.markFinished(now));

        assertEquals(ReservationStatus.COMPLETED, reservations.findById(pastRes.getId()).orElseThrow().getStatus());
        assertEquals(ReservationStatus.CONFIRMED, reservations.findById(inProgressRes.getId()).orElseThrow().getStatus());
        assertEquals(ReservationStatus.CONFIRMED, reservations.findById(upcomingRes.getId()).orElseThrow().getStatus());
        assertEquals(ReservationStatus.CANCELLED, reservations.findById(cancelledRes.getId()).orElseThrow().getStatus());
    }

    @Test
    void getStatisticsSummary_countsAndRevenueExcludeCancelled() {
        StatisticsSummary summary = reservations.getStatisticsSummary(from(), to());

        assertEquals(3L, summary.getReservationCount());
        assertEquals(1L, summary.getCancelledCount());
        assertEquals(0, new BigDecimal("615.00").compareTo(summary.getRevenue()));
    }

    @Test
    void getStatisticsSummary_emptyPeriodReturnsZeros() {
        StatisticsSummary summary = reservations.getStatisticsSummary(now.plusYears(1), now.plusYears(2));

        assertEquals(0L, summary.getReservationCount());
        assertEquals(0L, summary.getCancelledCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getRevenue()));
    }

    @Test
    void getTripOccupancy_calculatesCapacityAndPercent() {
        List<TripOccupancy> occupancy = reservations.getTripOccupancy(from(), to());

        assertEquals(1, occupancy.size());
        TripOccupancy o = occupancy.get(0);
        assertEquals(trip.getId(), o.getTripId());
        assertEquals("Plitvice", o.getName());
        assertEquals(3L, o.getSessionCount());
        assertEquals(30L, o.getCapacity());
        assertEquals(12L, o.getBookedPeople());
        assertEquals(40.0, o.getOccupancyPercent().doubleValue(), 0.001);
    }

    @Test
    void getTripOccupancy_includesSessionsWithoutReservations() {
        session(trip, now.plusDays(5));

        TripOccupancy o = reservations.getTripOccupancy(from(), to()).get(0);
        assertEquals(4L, o.getSessionCount());
        assertEquals(40L, o.getCapacity());
        assertEquals(30.0, o.getOccupancyPercent().doubleValue(), 0.001);
    }

    @Test
    void findTop5Categories_countsNonCancelledReservations() {
        List<TopItem> top = reservations.findTop5Categories(from(), to());

        assertEquals(1, top.size());
        assertEquals("Planinarenje", top.get(0).getName());
        assertEquals(3L, top.get(0).getTotal());
    }
}
