package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import touristAgency.models.AdditionalService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdditionalServiceRepositoryTest extends RepositoryTestBase{

    @Test
    void findByNameIgnoreCase() {
        assertEquals(transport.getId(), additionalServices.findByNameIgnoreCase("PRIJEVOZ").orElseThrow().getId());
        assertTrue(additionalServices.findByNameIgnoreCase("Ručak").isEmpty());
    }

    @Test
    void findAllByOrderByNameAsc_returnsAlphabetically() {
        List<String> names = additionalServices.findAllByOrderByNameAsc().stream()
                .map(AdditionalService::getName).toList();
        assertEquals(List.of("Najam opreme", "Prijevoz"), names);
    }

    @Test
    void findByTripId_returnsOnlyServicesOfferedForTrip() { 
        List<AdditionalService> offered = additionalServices.findByTripId(trip.getId());
        assertEquals(List.of(transport.getId()), offered.stream().map(AdditionalService::getId).toList());
    }

    @Test
    void findByTripId_returnsEmptyForTripWithoutServices() {
        assertTrue(additionalServices.findByTripId(-1L).isEmpty());
    }
}
