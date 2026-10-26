package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.Location;
import touristAgency.models.LocationType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByTripIdAndTypeOrderByNameAsc(Long tripId, LocationType type);

    Optional<Location> findFirstByTripIdAndType(Long tripId, LocationType type);

    List<Location> findByTripIdInAndType(Collection<Long> tripIds, LocationType type);

    List<Location> findByTripId(Long tripId);


    @Query("""
            SELECT l
            FROM Location l
            WHERE l.trip.id = :tripId
              AND l.type = touristAgency.models.LocationType.LANDMARK
              AND 6371000 * 2 * asin(sqrt(
                      power(sin(radians(l.latitude  - :lat) / 2), 2)
                    + cos(radians(:lat)) * cos(radians(l.latitude))
                    * power(sin(radians(l.longitude - :lon) / 2), 2)
                  )) <= :radiusMeters
            """)
    List<Location> findLandmarksNearby(@Param("tripId") Long tripId,
                                       @Param("lat") double lat,
                                       @Param("lon") double lon,
                                       @Param("radiusMeters") double radiusMeters);
}
