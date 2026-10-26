package touristAgency.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.Trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip,Long> {

    @EntityGraph(attributePaths = {"category", "additionalServices", "locations"})
    Optional<Trip> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = "category")
    List<Trip> findAllByOrderByNameAsc();

    default List<Trip> search(OffsetDateTime now,
                              OffsetDateTime dateFrom,
                              OffsetDateTime dateTo,
                              String location,
                              Long categoryId,
                              Integer minDuration,
                              Integer maxDuration,
                              BigDecimal minPrice,
                              BigDecimal maxPrice,
                              int requiredSeats) {
        OffsetDateTime from = (dateFrom == null) ? now : dateFrom;
        OffsetDateTime to = (dateTo == null) ? now.plusYears(100) : dateTo;
        String loc = (location == null) ? "" : location.trim();

        return searchQuery(now, from, to, loc, categoryId,
                minDuration, maxDuration, minPrice, maxPrice, requiredSeats);
    }

    @EntityGraph(attributePaths = "category")
    @Query("""
            select t from Trip t
            where (:categoryId  is null or t.category.id     =  :categoryId)
              and (:minDuration is null or t.durationMinutes >= :minDuration)
              and (:maxDuration is null or t.durationMinutes <= :maxDuration)
              and (:minPrice    is null or t.price           >= :minPrice)
              and (:maxPrice    is null or t.price           <= :maxPrice)
              and (:location = '' or exists (
                      select l from Location l
                      where l.trip = t
                        and l.type = touristAgency.models.LocationType.MAIN
                        and lower(l.name) like lower(concat('%', :location, '%'))))
              and exists (
                      select s from TripSession s
                      where s.trip = t
                        and s.startTime >  :now
                        and s.startTime >= :dateFrom
                        and s.startTime <  :dateTo
                        and t.maxParticipants - coalesce((
                                select sum(r.numberOfPeople) from Reservation r
                                where r.tripSession = s
                                  and r.status <> touristAgency.models.ReservationStatus.CANCELLED), 0)
                            >= :requiredSeats)
            order by t.name
            """)
    List<Trip> searchQuery(@Param("now") OffsetDateTime now,
                           @Param("dateFrom") OffsetDateTime dateFrom,
                           @Param("dateTo") OffsetDateTime dateTo,
                           @Param("location") String location,
                           @Param("categoryId") Long categoryId,
                           @Param("minDuration") Integer minDuration,
                           @Param("maxDuration") Integer maxDuration,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("requiredSeats") int requiredSeats);

}
