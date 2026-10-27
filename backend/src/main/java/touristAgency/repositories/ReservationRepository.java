package touristAgency.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    @Query("""
            select coalesce(sum(r.numberOfPeople), 0) from Reservation r
            where r.tripSession.id = :tripSessionId
              and r.status <> touristAgency.models.ReservationStatus.CANCELLED
            """)
    long sumBookedSeats(@Param("tripSessionId") Long tripSessionId);

    @EntityGraph(attributePaths = {"tripSession", "tripSession.trip",
            "reservationAdditionalServices", "reservationAdditionalServices.additionalService"})
    List<Reservation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "tripSession", "tripSession.trip",
            "reservationAdditionalServices", "reservationAdditionalServices.additionalService"})
    Optional<Reservation> findDetailsById(Long id);

    @EntityGraph(attributePaths = {"customer", "tripSession", "tripSession.trip"})
    @Query("""
            select r from Reservation r
            where (:tripId    is null or r.tripSession.trip.id = :tripId)
              and (:sessionId is null or r.tripSession.id      = :sessionId)
              and (:status    is null or r.status              = :status)
              and (:term      is null
                   or lower(r.customer.firstName) like lower(concat('%', :term, '%'))
                   or lower(r.customer.lastName)  like lower(concat('%', :term, '%'))
                   or lower(r.customer.email)     like lower(concat('%', :term, '%')))
            """)
    Page<Reservation> searchAdmin(@Param("tripId") Long tripId,
                                  @Param("sessionId") Long sessionId,
                                  @Param("status") ReservationStatus status,
                                  @Param("term") String term,
                                  Pageable pageable);

    @Query("""
            select r from Reservation r
            join fetch r.tripSession s
            join fetch s.trip
            where r.customer.id = :customerId
              and r.status = touristAgency.models.ReservationStatus.CONFIRMED
              and s.startTime <= :now
              and s.endTime   >  :now
            """)
    List<Reservation> findActiveInProgress(@Param("customerId") Long customerId,
                                           @Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Reservation r
            set r.status = touristAgency.models.ReservationStatus.COMPLETED,
                r.updatedAt = :now
            where r.status = touristAgency.models.ReservationStatus.CONFIRMED
              and r.tripSession in (select s from TripSession s where s.endTime <= :now)
            """)
    int markFinished(@Param("now") OffsetDateTime now);


    @Query("""
            select coalesce(sum(case when r.status <> touristAgency.models.ReservationStatus.CANCELLED then 1L else 0L end), 0L) as reservationCount,
                   coalesce(sum(case when r.status =  touristAgency.models.ReservationStatus.CANCELLED then 1L else 0L end), 0L)  as cancelledCount,
                   coalesce(sum(case when r.status <> touristAgency.models.ReservationStatus.CANCELLED then r.totalPrice end), 0)  as revenue
            from Reservation r
            where r.tripSession.startTime >= :dateFrom
              and r.tripSession.startTime <  :dateTo
            """)
    StatisticsSummary getStatisticsSummary(@Param("dateFrom") OffsetDateTime dateFrom,
                                           @Param("dateTo") OffsetDateTime dateTo);

    @Query("""
            select t.id as tripId, t.name as name, count(distinct s.id)  as sessionCount, count(distinct s.id) * t.maxParticipants as capacity,
             coalesce(sum(case when r.status <> touristAgency.models.ReservationStatus.CANCELLED then r.numberOfPeople else 0 end), 0) as bookedPeople,
              round(100.0 * coalesce(sum(case when r.status <> touristAgency.models.ReservationStatus.CANCELLED then r.numberOfPeople else 0 end), 0) / (count(distinct s.id) * t.maxParticipants), 1) as occupancyPercent
            from TripSession s
            join s.trip t
            left join Reservation r on r.tripSession = s
            where s.startTime >= :dateFrom
              and s.startTime <  :dateTo
              and s.active=true
            group by t.id, t.name, t.maxParticipants
            order by occupancyPercent desc, t.name
            """)
    List<TripOccupancy> getTripOccupancy(@Param("dateFrom") OffsetDateTime dateFrom,
                                         @Param("dateTo") OffsetDateTime dateTo);

    @Query("""
            select c.id as id, c.name as name, count(r) as total
            from Reservation r
            join r.tripSession s
            join s.trip t
            join t.category c
            where r.status <> touristAgency.models.ReservationStatus.CANCELLED
              and s.startTime >= :dateFrom
              and s.startTime <  :dateTo
            group by c.id, c.name
            order by count(r) desc, c.name
            limit 5
            """)
    List<TopItem> findTop5Categories(@Param("dateFrom") OffsetDateTime dateFrom,
                                     @Param("dateTo") OffsetDateTime dateTo);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Reservation r
        set r.status = touristAgency.models.ReservationStatus.CANCELLED,
            r.updatedAt = :now
        where r.tripSession.id = :sessionId
          and r.status = touristAgency.models.ReservationStatus.CONFIRMED
        """)
    int cancelAllForSession(@Param("sessionId") Long sessionId, @Param("now") OffsetDateTime now);
}
