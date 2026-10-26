package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.Location;
import touristAgency.models.LocationType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LocationRepositoryTest extends  RepositoryTestBase{
    @Test
    void findByTripIdAndType_returnsLandmarksSortedByName() {   // FR-20
        List<String> names = locations.findByTripIdAndTypeOrderByNameAsc(trip.getId(), LocationType.LANDMARK)
                .stream().map(Location::getName).toList();
        assertEquals(List.of("Kozjak", "Veliki slap"), names);
    }

    @Test
    void findFirstByTripIdAndType_returnsDeparture() {   // FR-10, FR-21
        Location departure = locations.findFirstByTripIdAndType(trip.getId(), LocationType.DEPARTURE).orElseThrow();
        assertEquals("Kolodvor Zagreb", departure.getName());
    }

    @Test
    void findByTripIdInAndType_returnsMainLocations() {   // FR-1
        List<Location> main = locations.findByTripIdInAndType(List.of(trip.getId()), LocationType.MAIN);
        assertEquals(List.of("NP Plitvička jezera"), main.stream().map(Location::getName).toList());
    }

    @Test
    void findByTripId_returnsAllLocationsOfTrip() {
        assertEquals(4, locations.findByTripId(trip.getId()).size());
    }

    @Test
    void findLandmarksNearby_returnsOnlyLandmarksWithin100m() {
        List<Location> nearby = locations.findLandmarksNearby(trip.getId(), 44.90245, 15.6100, 100);
        assertEquals(List.of("Veliki slap"), nearby.stream().map(Location::getName).toList());
    }

    @Test
    void findLandmarksNearby_returnsEmptyWhenFarAway() {
        assertTrue(locations.findLandmarksNearby(trip.getId(), 45.8150, 15.9819, 100).isEmpty());
    }

    @Test
    void findLandmarksNearby_ignoresMainAndDepartureLocations() {
        assertTrue(locations.findLandmarksNearby(trip.getId(), 44.8654, 15.5820, 100).isEmpty());
    }

    @Test
    void save_rejectsSecondMainLocationForSameTrip() {
        Location secondMain = location("Druga glavna", LocationType.MAIN, 44.0, 15.0);
        secondMain.setTrip(trip);
        assertThrows(DataIntegrityViolationException.class, () -> locations.saveAndFlush(secondMain));
    }
}
