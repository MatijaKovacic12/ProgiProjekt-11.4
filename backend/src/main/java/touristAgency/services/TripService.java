package touristAgency.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.dto.AdditionalServiceResponse;
import touristAgency.dto.CreateTripRequest;
import touristAgency.dto.CreateTripSessionRequest;
import touristAgency.dto.LandmarkRequest;
import touristAgency.dto.LocationResponse;
import touristAgency.dto.PriceCalculationResponse;
import touristAgency.dto.TripDetailsResponse;
import touristAgency.dto.TripSearchCriteria;
import touristAgency.dto.TripSessionResponse;
import touristAgency.dto.TripSummaryResponse;
import touristAgency.dto.UpdateTripRequest;
import touristAgency.exceptions.BadRequestException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.mappers.TripMapper;
import touristAgency.models.AdditionalService;
import touristAgency.models.Category;
import touristAgency.models.Location;
import touristAgency.models.LocationType;
import touristAgency.models.Trip;
import touristAgency.models.TripSession;
import touristAgency.repositories.AdditionalServiceRepository;
import touristAgency.repositories.CategoryRepository;
import touristAgency.repositories.LocationRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.TripRepository;
import touristAgency.repositories.TripSessionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final TripSessionRepository tripSessionRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final ReservationRepository reservationRepository;
    private final TripMapper tripMapper;

    // CONSTRUCTOR INJECTION (zahtjev iz zadatka):
    public TripService(TripRepository tripRepository,
                       CategoryRepository categoryRepository,
                       LocationRepository locationRepository,
                       TripSessionRepository tripSessionRepository,
                       AdditionalServiceRepository additionalServiceRepository,
                       ReservationRepository reservationRepository,
                       TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.tripSessionRepository = tripSessionRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.reservationRepository = reservationRepository;
        this.tripMapper = tripMapper;
    }

    @Transactional(readOnly = true)
    public List<TripSummaryResponse> getAllTrips() {
        List<Trip> trips = tripRepository.findByActiveTrueOrderByNameAsc();
        return trips.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<TripSummaryResponse> getTripsPage(Pageable pageable) {
        Page<Trip> trips = tripRepository.findByActiveTrueOrderByNameAsc(pageable);
        return trips.map(this::mapToSummaryResponse);
    }


    @Transactional(readOnly = true)
    public List<TripSummaryResponse> searchTrips(TripSearchCriteria criteria) {
        if (criteria == null) {
            return getAllTrips();
        }

        Long categoryId = criteria.getCategoryId();
        if (categoryId == null && criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            Optional<Category> catOpt = categoryRepository.findByNameIgnoreCase(criteria.getCategory().trim());
            if (catOpt.isPresent()) {
                categoryId = catOpt.get().getId();
            }
        }

        String locationQuery = criteria.getLocation() != null ? criteria.getLocation() : criteria.getSearch();
        int requiredSeats = criteria.getRequiredSeats() != null ? criteria.getRequiredSeats() : 1;

        List<Trip> trips = tripRepository.search(
                OffsetDateTime.now(),
                criteria.getDateFrom(),
                criteria.getDateTo(),
                locationQuery,
                categoryId,
                criteria.getMinDuration(),
                criteria.getMaxDuration(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                requiredSeats
        );

        return trips.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public TripDetailsResponse getTripById(Long id) {
        Trip trip = tripRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + id + " not found"));

        if (!trip.isActive()) {
            throw new ResourceNotFoundException("Trip with id " + id + " is not active");
        }

        String mainLocName = locationRepository.findFirstByTripIdAndType(id, LocationType.MAIN)
                .map(Location::getName)
                .orElse(null);

        String departurePoint = locationRepository.findFirstByTripIdAndType(id, LocationType.DEPARTURE)
                .map(Location::getName)
                .orElse(null);

        List<AdditionalServiceResponse> services = trip.getAdditionalServices().stream()
                .map(tripMapper::toAdditionalServiceResponse)
                .collect(Collectors.toList());

        List<TripSession> sessions = tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(id);
        List<TripSessionResponse> sessionResponses = sessions.stream()
                .map(s -> {
                    long booked = reservationRepository.sumBookedSeats(s.getId());
                    long available = Math.max(0, trip.getMaxParticipants() - booked);
                    return tripMapper.toTripSessionResponse(s, available);
                })
                .collect(Collectors.toList());

        List<LocationResponse> landmarks = locationRepository.findByTripIdAndTypeOrderByNameAsc(id, LocationType.LANDMARK)
                .stream()
                .map(tripMapper::toLocationResponse)
                .collect(Collectors.toList());

        return tripMapper.toDetailsResponse(trip, mainLocName, departurePoint, services, sessionResponses, landmarks);
    }


    public TripDetailsResponse createTrip(CreateTripRequest request) {
        validateCreateRequest(request);

        Trip trip = new Trip();
        trip.setName(request.resolveName());
        trip.setDescription(request.getDescription());

        Category category = resolveCategory(request.getCategoryId(), request.getActivityCategory());
        trip.setCategory(category);

        Integer duration = request.getDurationMinutes();
        if (duration == null && request.getDuration() != null) {
            duration = tripMapper.parseDurationToMinutes(request.getDuration());
        }
        if (duration == null || duration <= 0) {
            duration = 60;
        }
        trip.setDurationMinutes(duration);

        trip.setPrice(request.getPrice());
        trip.setMaxParticipants(request.getMaxParticipants());
        trip.setRequiredGuides(request.resolveRequiredGuides());
        trip.setActive(true);

        Set<AdditionalService> services = resolveAdditionalServices(
                request.getAdditionalServiceIds(),
                request.getAdditionalServices()
        );
        trip.setAdditionalServices(services);

        Trip savedTrip = tripRepository.save(trip);

        BigDecimal[] coords = parseCoordinates(request.getPoiCoordinates());


        String locationName = request.resolveLocationName();
        if (locationName != null && !locationName.isBlank()) {
            Location mainLoc = new Location();
            mainLoc.setName(locationName);
            mainLoc.setDescription(locationName);
            mainLoc.setSignificance("Main location");
            mainLoc.setLatitude(coords[0]);
            mainLoc.setLongitude(coords[1]);
            mainLoc.setType(LocationType.MAIN);
            mainLoc.setTrip(savedTrip);
            locationRepository.save(mainLoc);
        }


        if (request.getDeparturePoint() != null && !request.getDeparturePoint().isBlank()) {
            Location depLoc = new Location();
            depLoc.setName(request.getDeparturePoint());
            depLoc.setDescription(request.getDeparturePoint());
            depLoc.setSignificance("Departure point");
            depLoc.setLatitude(coords[0]);
            depLoc.setLongitude(coords[1]);
            depLoc.setType(LocationType.DEPARTURE);
            depLoc.setTrip(savedTrip);
            locationRepository.save(depLoc);
        }


        if (request.getLandmarks() != null) {
            for (LandmarkRequest lmReq : request.getLandmarks()) {
                Location lm = new Location();
                lm.setName(lmReq.getName());
                lm.setDescription(lmReq.getDescription());
                lm.setSignificance(lmReq.getSignificance());
                lm.setLatitude(lmReq.getLatitude());
                lm.setLongitude(lmReq.getLongitude());
                lm.setType(LocationType.LANDMARK);
                lm.setTrip(savedTrip);
                locationRepository.save(lm);
            }
        }


        if (request.getScheduledTimeSlots() != null) {
            for (OffsetDateTime slotTime : request.getScheduledTimeSlots()) {
                if (slotTime != null) {
                    TripSession session = new TripSession();
                    session.setTrip(savedTrip);
                    session.schedule(slotTime);
                    session.setActive(true);
                    tripSessionRepository.save(session);
                }
            }
        }

        return getTripById(savedTrip.getId());
    }


    public TripDetailsResponse updateTrip(Long id, UpdateTripRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + id + " not found"));

        if (!trip.isActive()) {
            throw new ResourceNotFoundException("Cannot update an inactive or deleted trip");
        }

        if (request.resolveName() != null) {
            trip.setName(request.resolveName());
        }
        if (request.getDescription() != null) {
            trip.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null || request.getActivityCategory() != null) {
            Category category = resolveCategory(request.getCategoryId(), request.getActivityCategory());
            trip.setCategory(category);
        }

        Integer duration = request.getDurationMinutes();
        if (duration == null && request.getDuration() != null) {
            duration = tripMapper.parseDurationToMinutes(request.getDuration());
        }
        if (duration != null && duration > 0) {
            trip.setDurationMinutes(duration);
        }

        if (request.getPrice() != null) {
            trip.setPrice(request.getPrice());
        }
        if (request.getMaxParticipants() != null) {
            trip.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.resolveRequiredGuides() != null) {
            trip.setRequiredGuides(request.resolveRequiredGuides());
        }

        if ((request.getAdditionalServiceIds() != null && !request.getAdditionalServiceIds().isEmpty())
                || (request.getAdditionalServices() != null && !request.getAdditionalServices().isEmpty())) {
            Set<AdditionalService> services = resolveAdditionalServices(
                    request.getAdditionalServiceIds(),
                    request.getAdditionalServices()
            );
            trip.setAdditionalServices(services);
        }

        Trip savedTrip = tripRepository.save(trip);


        String locationName = request.resolveLocationName();
        if (locationName != null && !locationName.isBlank()) {
            Optional<Location> mainLocOpt = locationRepository.findFirstByTripIdAndType(id, LocationType.MAIN);
            if (mainLocOpt.isPresent()) {
                Location mainLoc = mainLocOpt.get();
                mainLoc.setName(locationName);
                mainLoc.setDescription(locationName);
                locationRepository.save(mainLoc);
            } else {
                Location mainLoc = new Location();
                mainLoc.setName(locationName);
                mainLoc.setDescription(locationName);
                mainLoc.setLatitude(new BigDecimal("45.8150"));
                mainLoc.setLongitude(new BigDecimal("15.9819"));
                mainLoc.setType(LocationType.MAIN);
                mainLoc.setTrip(savedTrip);
                locationRepository.save(mainLoc);
            }
        }


        if (request.getDeparturePoint() != null && !request.getDeparturePoint().isBlank()) {
            Optional<Location> depLocOpt = locationRepository.findFirstByTripIdAndType(id, LocationType.DEPARTURE);
            if (depLocOpt.isPresent()) {
                Location depLoc = depLocOpt.get();
                depLoc.setName(request.getDeparturePoint());
                depLoc.setDescription(request.getDeparturePoint());
                locationRepository.save(depLoc);
            } else {
                Location depLoc = new Location();
                depLoc.setName(request.getDeparturePoint());
                depLoc.setDescription(request.getDeparturePoint());
                depLoc.setLatitude(new BigDecimal("45.8150"));
                depLoc.setLongitude(new BigDecimal("15.9819"));
                depLoc.setType(LocationType.DEPARTURE);
                depLoc.setTrip(savedTrip);
                locationRepository.save(depLoc);
            }
        }

        return getTripById(savedTrip.getId());
    }


    public void deleteTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + id + " not found"));

        if (!trip.isActive()) {
            throw new ResourceNotFoundException("Trip with id " + id + " is already inactive");
        }

        trip.setActive(false);
        tripRepository.save(trip);

        List<TripSession> sessions = tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(id);
        for (TripSession session : sessions) {
            session.setActive(false);
            tripSessionRepository.save(session);
        }
    }


    @Transactional(readOnly = true)
    public List<TripSessionResponse> getSessionsForTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + tripId + " not found"));

        List<TripSession> sessions = tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(tripId);
        return sessions.stream()
                .map(s -> {
                    long booked = reservationRepository.sumBookedSeats(s.getId());
                    long available = Math.max(0, trip.getMaxParticipants() - booked);
                    return tripMapper.toTripSessionResponse(s, available);
                })
                .collect(Collectors.toList());
    }


    public TripSessionResponse addSessionToTrip(Long tripId, CreateTripSessionRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + tripId + " not found"));

        if (!trip.isActive()) {
            throw new BadRequestException("Cannot add sessions to an inactive trip");
        }

        if (request.getStartTime() == null) {
            throw new BadRequestException("Start time is required");
        }

        if (request.getStartTime().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }

        if (tripSessionRepository.existsByTripIdAndStartTimeAndActiveTrue(tripId, request.getStartTime())) {
            throw new BadRequestException("A session already exists for this trip at the specified start time");
        }

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.schedule(request.getStartTime());
        if (request.getEndTime() != null && request.getEndTime().isAfter(request.getStartTime())) {
            session.setEndTime(request.getEndTime());
        }
        session.setActive(true);

        TripSession savedSession = tripSessionRepository.save(session);
        return tripMapper.toTripSessionResponse(savedSession, (long) trip.getMaxParticipants());
    }


    public void cancelSession(Long tripId, Long sessionId) {
        TripSession session = tripSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip session with id " + sessionId + " not found"));

        if (!session.getTrip().getId().equals(tripId)) {
            throw new BadRequestException("Session " + sessionId + " does not belong to trip " + tripId);
        }

        session.setActive(false);
        tripSessionRepository.save(session);
    }


    @Transactional(readOnly = true)
    public List<LocationResponse> getLandmarksForTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with id " + tripId + " not found");
        }
        return locationRepository.findByTripIdAndTypeOrderByNameAsc(tripId, LocationType.LANDMARK)
                .stream()
                .map(tripMapper::toLocationResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<LocationResponse> getLandmarksNearby(Long tripId, double lat, double lon, double radiusMeters) {
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip with id " + tripId + " not found");
        }
        return locationRepository.findLandmarksNearby(tripId, lat, lon, radiusMeters)
                .stream()
                .map(tripMapper::toLocationResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public PriceCalculationResponse calculatePrice(Long tripId, int participants, List<Long> serviceIds) {
        if (participants < 1) {
            throw new BadRequestException("Number of participants must be at least 1");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + tripId + " not found"));

        if (participants > trip.getMaxParticipants()) {
            throw new BadRequestException("Participants exceed maximum capacity of " + trip.getMaxParticipants());
        }

        BigDecimal baseTotal = trip.getPrice().multiply(BigDecimal.valueOf(participants));
        BigDecimal addonsTotalPerPerson = BigDecimal.ZERO;
        List<AdditionalServiceResponse> selectedServices = new ArrayList<>();

        if (serviceIds != null && !serviceIds.isEmpty()) {
            List<AdditionalService> services = additionalServiceRepository.findAllById(serviceIds);
            for (AdditionalService s : services) {
                addonsTotalPerPerson = addonsTotalPerPerson.add(s.getPrice());
                selectedServices.add(tripMapper.toAdditionalServiceResponse(s));
            }
        }

        BigDecimal addonsTotal = addonsTotalPerPerson.multiply(BigDecimal.valueOf(participants));
        BigDecimal grandTotal = baseTotal.add(addonsTotal);

        return PriceCalculationResponse.builder()
                .tripId(trip.getId())
                .tripName(trip.getName())
                .numberOfParticipants(participants)
                .pricePerPerson(trip.getPrice())
                .baseTotal(baseTotal)
                .addonsTotalPerPerson(addonsTotalPerPerson)
                .addonsTotal(addonsTotal)
                .grandTotal(grandTotal)
                .selectedServices(selectedServices)
                .build();
    }

    private TripSummaryResponse mapToSummaryResponse(Trip trip) {
        String mainLoc = locationRepository.findFirstByTripIdAndType(trip.getId(), LocationType.MAIN)
                .map(Location::getName)
                .orElse(null);

        String departure = locationRepository.findFirstByTripIdAndType(trip.getId(), LocationType.DEPARTURE)
                .map(Location::getName)
                .orElse(null);

        List<TripSession> sessions = tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(trip.getId());
        List<String> timeSlots = sessions.stream()
                .map(s -> s.getStartTime() != null ? s.getStartTime().toString() : "")
                .collect(Collectors.toList());

        return tripMapper.toSummaryResponse(trip, mainLoc, departure, timeSlots);
    }

    private void validateCreateRequest(CreateTripRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        if (request.resolveName() == null || request.resolveName().isBlank()) {
            throw new BadRequestException("Trip name/title is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BadRequestException("Trip description is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Valid price is required");
        }
        if (request.getMaxParticipants() == null || request.getMaxParticipants() < 1) {
            throw new BadRequestException("Max participants must be at least 1");
        }
    }

    private Category resolveCategory(Long categoryId, String categoryName) {
        if (categoryId != null) {
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category with id " + categoryId + " not found"));
        }
        if (categoryName != null && !categoryName.isBlank()) {
            return categoryRepository.findByNameIgnoreCase(categoryName.trim())
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(categoryName.trim());
                        return categoryRepository.save(newCat);
                    });
        }
        throw new BadRequestException("Category id or category name must be provided");
    }

    private Set<AdditionalService> resolveAdditionalServices(List<Long> serviceIds, List<String> serviceNames) {
        Set<AdditionalService> services = new HashSet<>();
        if (serviceIds != null && !serviceIds.isEmpty()) {
            services.addAll(additionalServiceRepository.findAllById(serviceIds));
        }
        if (serviceNames != null && !serviceNames.isEmpty()) {
            for (String name : serviceNames) {
                additionalServiceRepository.findByNameIgnoreCase(name.trim()).ifPresent(services::add);
            }
        }
        return services;
    }

    private BigDecimal[] parseCoordinates(String poiCoordinates) {
        BigDecimal defaultLat = new BigDecimal("45.8150");
        BigDecimal defaultLon = new BigDecimal("15.9819");

        if (poiCoordinates == null || poiCoordinates.isBlank()) {
            return new BigDecimal[]{defaultLat, defaultLon};
        }

        try {
            String[] parts = poiCoordinates.split(",");
            if (parts.length >= 2) {
                BigDecimal lat = new BigDecimal(parts[0].trim());
                BigDecimal lon = new BigDecimal(parts[1].trim());
                return new BigDecimal[]{lat, lon};
            }
        } catch (Exception ignored) {
        }

        return new BigDecimal[]{defaultLat, defaultLon};
    }
}