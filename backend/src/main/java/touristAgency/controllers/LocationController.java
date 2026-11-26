package touristAgency.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.services.LocationService;

import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/locations")
    public List<String> getLocations() {
        return locationService.getMainLocations();
    }
}
