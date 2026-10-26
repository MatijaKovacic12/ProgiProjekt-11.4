package touristAgency.repositories.projection;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;

import java.awt.print.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    @Query("""
            select coalesce(sum(r.numberOfPeople), 0) from Reservation r
            where r.tripSession.id = :sessionId
              and r.status <> touristAgency.models.ReservationStatus.CANCELLED
            """)
    long sumBookedSeats(@Param("sessionId") Long sessionId);


    @EntityGraph(attributePaths = {"tripSession", "tripSession.trip", "reservationAdditionalServices", "reservationAdditionalServices.addOn"})
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "tripSession", "tripSession.trip", "reservationAdditionalServices", "reservationAdditionalServices.addOn"})
    Optional<Reservation> findDetailsById(Long id);



    @EntityGraph(attributePaths = {"user", "tripSession", "tripSession.trip"})
    @Query("""
            select r from Reservation r
            where (:tripId    is null or r.tripSession.trip.id = :tripId)
              and (:sessionId is null or r.tripSession.id      = :sessionId)
              and (:status    is null or r.status          = :status)
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
            where r.customer.id = :userId
              and r.status = touristAgency.models.ReservationStatus.CONFIRMED
              and s.startTime <= :now
              and s.endTime   >  :now
            """)
    List<Reservation> findActiveInProgress(@Param("userId") Long userId,
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


    @Query(nativeQuery = true, value = """
            SELECT COUNT(*) FILTER (WHERE r.status <> 'OTKAZANA')                         AS "reservationCount",
                   COUNT(*) FILTER (WHERE r.status =  'OTKAZANA')                         AS "cancelledCount",
                   COALESCE(SUM(r.ukupna_cijena) FILTER (WHERE r.status <> 'OTKAZANA'), 0) AS revenue
            FROM rezervacija r
            JOIN izlet_instanca ii ON ii.id = r.id_instanca
            WHERE ii.termin_pocetak >= :dateFrom AND ii.termin_pocetak < :dateTo
            """)
    StatisticsSummary getStatisticsSummary(@Param("dateFrom") OffsetDateTime dateFrom,
                                           @Param("dateTo") OffsetDateTime dateTo);

    @Query(nativeQuery = true, value = """
            SELECT x."tripId", x.name, x."sessionCount", x.capacity, x."bookedPeople",
                   ROUND(100.0 * x."bookedPeople" / NULLIF(x.capacity, 0), 1) AS "occupancyPercent"
            FROM (
                SELECT i.id                                    AS "tripId",
                       i.naziv                                 AS name,
                       COUNT(DISTINCT ii.id)                   AS "sessionCount",
                       COUNT(DISTINCT ii.id) * i.max_sudionika AS capacity,
                       COALESCE((SELECT SUM(r.broj_osoba)
                                 FROM rezervacija r
                                 JOIN izlet_instanca ii2 ON ii2.id = r.id_instanca
                                 WHERE ii2.id_izlet = i.id
                                   AND r.status <> 'OTKAZANA'
                                   AND ii2.termin_pocetak >= :dateFrom AND ii2.termin_pocetak < :dateTo), 0) AS "bookedPeople"
                FROM izlet i
                JOIN izlet_instanca ii ON ii.id_izlet = i.id
                WHERE ii.termin_pocetak >= :dateFrom AND ii.termin_pocetak < :dateTo
                GROUP BY i.id, i.naziv, i.max_sudionika
            ) x
            ORDER BY "occupancyPercent" DESC NULLS LAST, x.name
            """)
    List<TripOccupancy> getTripOccupancy(@Param("dateFrom") OffsetDateTime dateFrom,
                                         @Param("dateTo") OffsetDateTime dateTo);


    @Query(nativeQuery = true, value = """
            SELECT k.id AS id, k.ime AS name, COUNT(r.id) AS total
            FROM rezervacija r
            JOIN izlet_instanca ii ON ii.id = r.id_instanca
            JOIN izlet i           ON i.id  = ii.id_izlet
            JOIN kategorija k      ON k.id  = i.id_kategorija
            WHERE r.status <> 'OTKAZANA'
              AND ii.termin_pocetak >= :dateFrom AND ii.termin_pocetak < :dateTo
            GROUP BY k.id, k.ime
            ORDER BY total DESC, k.ime
            LIMIT 5
            """)
    List<TopItem> findTop5Categories(@Param("dateFrom") OffsetDateTime dateFrom,
                                     @Param("dateTo") OffsetDateTime dateTo);
}
