package touristAgency.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.dto.CreateTripRequest;
import touristAgency.dto.CreateTripSessionRequest;
import touristAgency.dto.LocationResponse;
import touristAgency.dto.PriceCalculationResponse;
import touristAgency.dto.TripDetailsResponse;
import touristAgency.dto.TripSearchCriteria;
import touristAgency.dto.TripSessionResponse;
import touristAgency.dto.TripSummaryResponse;
import touristAgency.dto.UpdateTripRequest;
import touristAgency.services.TripService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/trips", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService tripService;

    // CONSTRUCTOR INJECTION (zahtjev iz zadatka):
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }


    @GetMapping
    public ResponseEntity<List<TripSummaryResponse>> getAllTrips(@ModelAttribute TripSearchCriteria criteria) {
        boolean hasFilters = criteria != null && (
                criteria.getSearch() != null ||
                        criteria.getLocation() != null ||
                        criteria.getCategoryId() != null ||
                        criteria.getCategory() != null ||
                        criteria.getDateFrom() != null ||
                        criteria.getDateTo() != null ||
                        criteria.getMinDuration() != null ||
                        criteria.getMaxDuration() != null ||
                        criteria.getMinPrice() != null ||
                        criteria.getMaxPrice() != null ||
                        (criteria.getRequiredSeats() != null && criteria.getRequiredSeats() > 1)
        );

        List<TripSummaryResponse> trips = hasFilters
                ? tripService.searchTrips(criteria)
                : tripService.getAllTrips();

        return ResponseEntity.ok(trips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDetailsResponse> getTripById(@PathVariable Long id) {
        TripDetailsResponse tripDetails = tripService.getTripById(id);
        return ResponseEntity.ok(tripDetails);
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripDetailsResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        TripDetailsResponse createdTrip = tripService.createTrip(request);
        URI locationUri = URI.create("/api/trips/" + createdTrip.getId());
        return ResponseEntity.created(locationUri).body(createdTrip);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripDetailsResponse> updateTrip(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateTripRequest request) {
        TripDetailsResponse updatedTrip = tripService.updateTrip(id, request);
        return ResponseEntity.ok(updatedTrip);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<TripSessionResponse>> getTripSessions(@PathVariable Long id) {
        List<TripSessionResponse> sessions = tripService.getSessionsForTrip(id);
        return ResponseEntity.ok(sessions);
    }


    @PostMapping(value = "/{id}/sessions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripSessionResponse> addTripSession(@PathVariable Long id,
                                                              @Valid @RequestBody CreateTripSessionRequest request) {
        TripSessionResponse createdSession = tripService.addSessionToTrip(id, request);
        URI sessionUri = URI.create("/api/trips/" + id + "/sessions/" + createdSession.getId());
        return ResponseEntity.created(sessionUri).body(createdSession);
    }

    @DeleteMapping("/{id}/sessions/{sessionId}")
    public ResponseEntity<Void> cancelTripSession(@PathVariable Long id,
                                                  @PathVariable Long sessionId) {
        tripService.cancelSession(id, sessionId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/landmarks")
    public ResponseEntity<List<LocationResponse>> getLandmarks(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "100.0") Double radiusMeters) {

        List<LocationResponse> landmarks = (lat != null && lon != null)
                ? tripService.getLandmarksNearby(id, lat, lon, radiusMeters)
                : tripService.getLandmarksForTrip(id);

        return ResponseEntity.ok(landmarks);
    }


    @GetMapping("/{id}/calculate-price")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int participants,
            @RequestParam(required = false) List<Long> serviceIds) {

        PriceCalculationResponse calculation = tripService.calculatePrice(id, participants, serviceIds);
        return ResponseEntity.ok(calculation);
    }
}