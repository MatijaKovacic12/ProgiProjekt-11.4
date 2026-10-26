package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import touristAgency.models.TripSessionGuide;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TripSessionGuideRepositoryTest extends RepositoryTestBase{
    @Test
    void findByTripSessionId_returnsAssignedGuides() {
        List<TripSessionGuide> assigned = tripSessionGuides.findByTripSessionId(upcoming.getId());

        assertEquals(1, assigned.size());
        assertEquals(guide1.getId(), assigned.get(0).getGuide().getId());
    }

    @Test
    void countByTripSessionId() {
        assertEquals(1, tripSessionGuides.countByTripSessionId(upcoming.getId()));
        assertEquals(0, tripSessionGuides.countByTripSessionId(past.getId()));
    }

    @Test
    void hasOverlap_trueWhenIntervalsOverlap() {
        assertTrue(tripSessionGuides.hasOverlap(guide1.getId(),
                upcoming.getStartTime().plusHours(1), upcoming.getEndTime().plusHours(1)));
        assertTrue(tripSessionGuides.hasOverlap(guide1.getId(),
                upcoming.getStartTime().minusHours(1), upcoming.getEndTime().plusHours(1)));
        assertTrue(tripSessionGuides.hasOverlap(guide1.getId(),
                upcoming.getStartTime(), upcoming.getEndTime()));
    }

    @Test
    void hasOverlap_falseWhenIntervalsOnlyTouchOrAreApart() {
        assertFalse(tripSessionGuides.hasOverlap(guide1.getId(),
                upcoming.getEndTime(), upcoming.getEndTime().plusHours(2)));
        assertFalse(tripSessionGuides.hasOverlap(guide1.getId(),
                upcoming.getStartTime().minusHours(2), upcoming.getStartTime()));
        assertFalse(tripSessionGuides.hasOverlap(guide2.getId(),
                upcoming.getStartTime(), upcoming.getEndTime()));
    }

    @Test
    void findAssignmentsForGuide_returnsSessionsWithTrip() {
        List<TripSessionGuide> assignments = tripSessionGuides.findAssignmentsForGuide(guide1.getId());

        assertEquals(1, assignments.size());
        assertEquals("Plitvice", assignments.get(0).getTripSession().getTrip().getName());
        assertTrue(tripSessionGuides.findAssignmentsForGuide(guide2.getId()).isEmpty());
    }

    @Test
    void findUpcomingAssignments_ignoresPastSessions() {
        tripSessionGuides.save(new TripSessionGuide(past, guide2));

        List<TripSessionGuide> upcomingAssignments = tripSessionGuides.findUpcomingAssignments(now);
        assertEquals(1, upcomingAssignments.size());
        assertEquals(guide1.getId(), upcomingAssignments.get(0).getGuide().getId());
    }

    @Test
    void deleteUpcomingAssignments_removesOnlyFutureAssignmentsOfGuide() {
        tripSessionGuides.save(new TripSessionGuide(past, guide1));
        tripSessionGuides.flush();

        assertEquals(1, tripSessionGuides.deleteUpcomingAssignments(guide1.getId(), now));
        assertEquals(0, tripSessionGuides.countByTripSessionId(upcoming.getId()));
        assertEquals(1, tripSessionGuides.countByTripSessionId(past.getId()));
    }
}
