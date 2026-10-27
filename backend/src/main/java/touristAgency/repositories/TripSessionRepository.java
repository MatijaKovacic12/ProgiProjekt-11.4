package touristAgency.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import touristAgency.models.TripSession;
import touristAgency.repositories.projection.TripSessionAvailability;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TripSessionRepository extends JpaRepository<TripSession, Long> {

    List<TripSession> findByTripIdOrderByStartTimeAsc(Long tripId);

    List<TripSession> findByTripIdAndActiveTrueOrderByStartTimeAsc(Long tripId);

    boolean existsByTripIdAndStartTimeAndActiveTrue(Long tripId, OffsetDateTime startTime);

    @EntityGraph(attributePaths = "trip")
    Optional<TripSession> findWithTripById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TripSession s join fetch s.trip where s.id = :id")
    Optional<TripSession> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select s.id        as sessionId,
                   t.id        as tripId,
                   s.startTime as startTime,
                   s.endTime   as endTime,
                   t.maxParticipants - coalesce((
                       select sum(r.numberOfPeople) from Reservation r
                       where r.tripSession = s
                         and r.status <> touristAgency.models.ReservationStatus.CANCELLED), 0) as availableSeats
            from TripSession s join s.trip t
            where t.id in :tripIds
              and s.active = true
              and t.active = true
              and s.startTime > :now
            order by t.id, s.startTime
            """)
    List<TripSessionAvailability> findUpcomingWithAvailability(@Param("tripIds") Collection<Long> tripIds,
                                                               @Param("now") OffsetDateTime now);
}
