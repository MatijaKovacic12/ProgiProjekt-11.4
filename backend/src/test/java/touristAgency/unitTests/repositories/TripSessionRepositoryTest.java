package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.TripSession;
import touristAgency.repositories.projection.TripSessionAvailability;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TripSessionRepositoryTest extends RepositoryTestBase{
    @Test
    void schedule_setsEndTimeFromTripDuration() {
        assertEquals(upcoming.getStartTime().plusMinutes(240), upcoming.getEndTime());
    }

    @Test
    void findByTripIdOrderByStartTimeAsc_returnsSessionsChronologically() {
        List<Long> ids = tripSessions.findByTripIdOrderByStartTimeAsc(trip.getId())
                .stream().map(TripSession::getId).toList();
        assertEquals(List.of(past.getId(), inProgress.getId(), upcoming.getId()), ids);
    }

    @Test
    void existsByTripIdAndStartTime() {
        assertTrue(tripSessions.existsByTripIdAndStartTime(trip.getId(), upcoming.getStartTime()));
        assertFalse(tripSessions.existsByTripIdAndStartTime(trip.getId(), upcoming.getStartTime().plusMinutes(1)));
    }

    @Test
    void save_rejectsSecondSessionAtSameStartTime() {
        TripSession duplicate = new TripSession();
        duplicate.setTrip(trip);
        duplicate.schedule(upcoming.getStartTime());
        assertThrows(DataIntegrityViolationException.class, () -> tripSessions.saveAndFlush(duplicate));
    }

    @Test
    void findWithTripById_loadsTrip() {
        TripSession loaded = tripSessions.findWithTripById(upcoming.getId()).orElseThrow();
        assertEquals("Plitvice", loaded.getTrip().getName());
    }

    @Test
    void findByIdForUpdate_returnsSessionWithTrip() {
        TripSession locked = tripSessions.findByIdForUpdate(upcoming.getId()).orElseThrow();
        assertEquals(10, locked.getTrip().getMaxParticipants());
    }

    @Test
    void findUpcomingWithAvailability_returnsOnlyFutureSessionsWithFreeSeats() {
        List<TripSessionAvailability> result =
                tripSessions.findUpcomingWithAvailability(List.of(trip.getId()), now);

        assertEquals(1, result.size());
        TripSessionAvailability a = result.get(0);
        assertEquals(upcoming.getId(), a.getSessionId());
        assertEquals(trip.getId(), a.getTripId());
        assertNotNull(a.getStartTime());
        assertEquals(3L, a.getAvailableSeats());
    }
}
