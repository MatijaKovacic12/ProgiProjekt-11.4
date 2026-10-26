package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.ReservationAdditionalService;
import touristAgency.models.ReservationAdditionalServiceId;
import touristAgency.repositories.projection.TopItem;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationAdditionalServiceRepository  extends JpaRepository<ReservationAdditionalService, ReservationAdditionalServiceId> {

    @Query("""
            select ra from ReservationAdditionalService ra
            join fetch ra.additionalService
            where ra.reservation.id = :reservationId
            """)
    List<ReservationAdditionalService> findByReservationId(@Param("reservationId") Long reservationId);

    @Query("""
            select a.id as id, a.name as name, sum(ra.quantity) as total
            from ReservationAdditionalService ra
            join ra.reservation r
            join r.tripSession s
            join ra.additionalService a
            where r.status <> touristAgency.models.ReservationStatus.CANCELLED
              and s.startTime >= :dateFrom
              and s.startTime <  :dateTo
            group by a.id, a.name
            order by sum(ra.quantity) desc, a.name
            limit 5
            """)
    List<TopItem> findTop5AdditionalServices(@Param("dateFrom") OffsetDateTime dateFrom,
                                             @Param("dateTo") OffsetDateTime dateTo);


}

