package touristAgency.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.dto.AdditionalServiceResponse;
import touristAgency.dto.CategoryResponse;
import touristAgency.services.CatalogService;

import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return catalogService.getCategories();
    }

    @GetMapping("/additional-services")
    public List<AdditionalServiceResponse> getAdditionalServices() {
        return catalogService.getAdditionalServices();
    }
}
