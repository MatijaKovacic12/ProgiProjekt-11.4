package touristAgency.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.dto.AdditionalServiceResponse;
import touristAgency.dto.CategoryResponse;
import touristAgency.mappers.TripMapper;
import touristAgency.repositories.AdditionalServiceRepository;
import touristAgency.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final TripMapper tripMapper;

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(c -> CategoryResponse.builder().id(c.getId()).name(c.getName()).build())
                .toList();
    }

    public List<AdditionalServiceResponse> getAdditionalServices() {
        return additionalServiceRepository.findAllByOrderByNameAsc().stream()
                .map(tripMapper::toAdditionalServiceResponse)
                .toList();
    }
}
