package touristAgency.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.models.LocationType;
import touristAgency.repositories.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    public List<String> getMainLocations() {
        return locationRepository.findDistinctNamesByType(LocationType.MAIN);
    }
}
