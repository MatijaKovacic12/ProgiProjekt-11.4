package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import touristAgency.models.Trip;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TripRepositoryTest extends  RepositoryTestBase{
    private List<Trip> searchAll(int requiredSeats) {
        return trips.search(now, null, null, null, null, null, null, null, null, requiredSeats);
    }

    @Test
    void findWithDetailsById_loadsCategoryServicesAndLocations() {   // FR-3
        Trip loaded = trips.findWithDetailsById(trip.getId()).orElseThrow();

        assertEquals("Planinarenje", loaded.getCategory().getName());
        assertEquals(1, loaded.getAdditionalServices().size());
        assertEquals(4, loaded.getLocations().size());
    }

    @Test
    void findAllByOrderByNameAsc() {
        assertEquals(List.of("Plitvice"), trips.findAllByOrderByNameAsc().stream().map(Trip::getName).toList());
    }


    @Test
    void search_withoutFilters_returnsTripWithUpcomingSession() {
        assertEquals(1, searchAll(1).size());
    }

    @Test
    void search_withAllMatchingFilters_returnsTrip() {
        List<Trip> result = trips.search(now, now, now.plusDays(7), "plitvi", hiking.getId(),
                60, 300, new BigDecimal("10"), new BigDecimal("100"), 3);
        assertEquals(1, result.size());
    }

    @Test
    void search_requiredSeats_countsOnlyNonCancelledReservations() {
        // max 10, potvrđeno 7, otkazano 2 -> slobodno 3
        assertEquals(1, searchAll(3).size());
        assertEquals(0, searchAll(4).size());
    }

    @Test
    void search_byLocation_matchesMainLocationOnly() {
        assertEquals(1, trips.search(now, null, null, "PLITVI", null, null, null, null, null, 1).size());
        // "Kolodvor" je polazište, ne glavna lokacija
        assertEquals(0, trips.search(now, null, null, "Kolodvor", null, null, null, null, null, 1).size());
    }

    @Test
    void search_byDateRange_excludesTripWithoutSessionsInRange() {
        assertEquals(0, trips.search(now, now.plusDays(5), null, null, null, null, null, null, null, 1).size());
        assertEquals(0, trips.search(now, null, now.plusDays(1), null, null, null, null, null, null, 1).size());
    }

    @Test
    void search_byCategoryDurationAndPrice_filtersOut() {
        assertEquals(0, trips.search(now, null, null, null, -1L, null, null, null, null, 1).size());
        assertEquals(0, trips.search(now, null, null, null, null, 300, null, null, null, 1).size());
        assertEquals(0, trips.search(now, null, null, null, null, null, 120, null, null, 1).size());
        assertEquals(0, trips.search(now, null, null, null, null, null, null, new BigDecimal("60"), null, 1).size());
        assertEquals(0, trips.search(now, null, null, null, null, null, null, null, new BigDecimal("40"), 1).size());
    }

    @Test
    void search_ignoresTripsWithOnlyPastSessions() {
        Trip old = new Trip();
        old.setName("Stari izlet");
        old.setDescription("Samo prošli termini");
        old.setCategory(hiking);
        old.setDurationMinutes(60);
        old.setPrice(new BigDecimal("10.00"));
        old.setMaxParticipants(10);
        trips.save(old);
        session(old, now.minusDays(10));

        List<String> names = searchAll(1).stream().map(Trip::getName).toList();
        assertEquals(List.of("Plitvice"), names);
    }
}
