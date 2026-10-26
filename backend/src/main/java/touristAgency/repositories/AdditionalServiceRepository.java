package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import touristAgency.models.AdditionalService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdditionalServiceRepository extends JpaRepository<AdditionalService,Long> {

    Optional<AdditionalService> findByNameIgnoreCase(String name);

    List<AdditionalService> findAllByOrderByNameAsc();

    @Query("select a from Trip t join t.additionalServices a where t.id = :tripId order by a.name")
    List<AdditionalService> findByTripId(@Param("tripId") Long tripId);
}
