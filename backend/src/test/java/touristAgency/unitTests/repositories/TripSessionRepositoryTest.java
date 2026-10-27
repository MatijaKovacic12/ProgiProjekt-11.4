package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.TripSession;
import touristAgency.repositories.projection.TripSessionAvailability;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TripSessionRepositoryTest extends RepositoryTestBase {

    @Test
    void schedule_setsEndTimeFromTripDuration() {
        assertEquals(upcoming.getStartTime().plusMinutes(240), upcoming.getEndTime());
    }

    @Test
    void save_setsActiveByDefault() {
        assertTrue(upcoming.isActive());
    }

    @Test
    void findByTripIdAndActiveTrue_returnsSessionsChronologically() {
        List<Long> ids = tripSessions.findByTripIdAndActiveTrueOrderByStartTimeAsc(trip.getId())
                .stream().map(TripSession::getId).toList();
        assertEquals(List.of(past.getId(), inProgress.getId(), upcoming.getId()), ids);
    }

    @Test
    void findByTripIdAndActiveTrue_excludesInactiveSessions() {
        past.setActive(false);
        tripSessions.saveAndFlush(past);

        List<Long> ids = tripSessions.findByTripIdAndActiveTrueOrderByStartTimeAsc(trip.getId())
                .stream().map(TripSession::getId).toList();
        assertEquals(List.of(inProgress.getId(), upcoming.getId()), ids);
    }

    @Test
    void existsByTripIdAndStartTimeAndActiveTrue() {
        assertTrue(tripSessions.existsByTripIdAndStartTimeAndActiveTrue(trip.getId(), upcoming.getStartTime()));
        assertFalse(tripSessions.existsByTripIdAndStartTimeAndActiveTrue(trip.getId(), upcoming.getStartTime().plusMinutes(1)));
    }

    @Test
    void existsByTripIdAndStartTimeAndActiveTrue_ignoresInactiveSession() {
        upcoming.setActive(false);
        tripSessions.saveAndFlush(upcoming);

        assertFalse(tripSessions.existsByTripIdAndStartTimeAndActiveTrue(trip.getId(), upcoming.getStartTime()));
    }

    @Test
    void save_rejectsSecondActiveSessionAtSameStartTime() {
        TripSession duplicate = new TripSession();
        duplicate.setTrip(trip);
        duplicate.schedule(upcoming.getStartTime());
        assertThrows(DataIntegrityViolationException.class, () -> tripSessions.saveAndFlush(duplicate));
    }

    @Test
    void save_allowsNewSessionAtSameStartTimeAfterDeactivation() {
        upcoming.setActive(false);
        tripSessions.saveAndFlush(upcoming);

        TripSession replacement = new TripSession();
        replacement.setTrip(trip);
        replacement.schedule(upcoming.getStartTime());

        assertDoesNotThrow(() -> tripSessions.saveAndFlush(replacement));
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

    @Test
    void findUpcomingWithAvailability_excludesInactiveSession() {
        upcoming.setActive(false);
        tripSessions.saveAndFlush(upcoming);

        assertTrue(tripSessions.findUpcomingWithAvailability(List.of(trip.getId()), now).isEmpty());
    }

    @Test
    void findUpcomingWithAvailability_excludesSessionsOfInactiveTrip() {
        trip.setActive(false);
        trips.saveAndFlush(trip);

        assertTrue(tripSessions.findUpcomingWithAvailability(List.of(trip.getId()), now).isEmpty());
    }
}
