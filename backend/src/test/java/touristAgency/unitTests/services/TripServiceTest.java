package touristAgency.unitTests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import touristAgency.dto.CreateTripRequest;
import touristAgency.dto.CreateTripSessionRequest;
import touristAgency.dto.PriceCalculationResponse;
import touristAgency.dto.TripDetailsResponse;
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
import touristAgency.services.TripService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TripSessionRepository tripSessionRepository;

    @Mock
    private AdditionalServiceRepository additionalServiceRepository;

    @Mock
    private ReservationRepository reservationRepository;


    @Spy
    private TripMapper tripMapper = new TripMapper();

    @InjectMocks
    private TripService tripService;

    private Category testCategory;
    private Trip testTrip;
    private Location testMainLocation;
    private Location testDepartureLocation;
    private AdditionalService testService;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("City tour");

        testService = new AdditionalService();
        testService.setId(10L);
        testService.setName("Audio guide");
        testService.setPrice(new BigDecimal("8.00"));

        testTrip = new Trip();
        testTrip.setId(100L);
        testTrip.setName("Zagreb Historic Walking Tour");
        testTrip.setDescription("A guided walking tour through Zagreb.");
        testTrip.setCategory(testCategory);
        testTrip.setDurationMinutes(150);
        testTrip.setPrice(new BigDecimal("25.00"));
        testTrip.setMaxParticipants(18);
        testTrip.setRequiredGuides(1);
        testTrip.setActive(true);
        testTrip.setAdditionalServices(new HashSet<>(Set.of(testService)));

        testMainLocation = new Location();
        testMainLocation.setId(201L);
        testMainLocation.setName("Zagreb, Croatia");
        testMainLocation.setType(LocationType.MAIN);
        testMainLocation.setTrip(testTrip);
        testMainLocation.setLatitude(new BigDecimal("45.8131"));
        testMainLocation.setLongitude(new BigDecimal("15.9775"));

        testDepartureLocation = new Location();
        testDepartureLocation.setId(202L);
        testDepartureLocation.setName("Ban Jelačić Square, Zagreb");
        testDepartureLocation.setType(LocationType.DEPARTURE);
        testDepartureLocation.setTrip(testTrip);
        testDepartureLocation.setLatitude(new BigDecimal("45.8131"));
        testDepartureLocation.setLongitude(new BigDecimal("15.9775"));
    }

    @Test
    @DisplayName("getAllTrips vraća mapirane aktivne izlete")
    void getAllTrips_returnsListOfTrips() {
        when(tripRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(testTrip));
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.MAIN))
                .thenReturn(Optional.of(testMainLocation));
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.DEPARTURE))
                .thenReturn(Optional.of(testDepartureLocation));
        when(tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(100L))
                .thenReturn(Collections.emptyList());

        List<TripSummaryResponse> result = tripService.getAllTrips();

        assertThat(result).hasSize(1);
        TripSummaryResponse item = result.get(0);
        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getTitle()).isEqualTo("Zagreb Historic Walking Tour");
        assertThat(item.getActivityCategory()).isEqualTo("City tour");
        assertThat(item.getDuration()).isEqualTo("2.5 hours");
        assertThat(item.getLocation()).isEqualTo("Zagreb, Croatia");
        assertThat(item.getDeparturePoint()).isEqualTo("Ban Jelačić Square, Zagreb");
        assertThat(item.getPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("getTripsPage vraća traženu stranicu mapiranih izleta")
    void getTripsPage_returnsPageOfTrips() {
        Pageable pageable = PageRequest.of(1, 15);
        Page<Trip> tripPage = new PageImpl<>(List.of(testTrip), pageable, 16);

        when(tripRepository.findByActiveTrueOrderByNameAsc(pageable)).thenReturn(tripPage);
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.MAIN))
                .thenReturn(Optional.of(testMainLocation));
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.DEPARTURE))
                .thenReturn(Optional.of(testDepartureLocation));
        when(tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(100L))
                .thenReturn(Collections.emptyList());

        Page<TripSummaryResponse> result = tripService.getTripsPage(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Zagreb Historic Walking Tour");
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(16);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("getTripById vraća pune detalje i točan broj slobodnih mjesta")
    void getTripById_whenTripExists_returnsTripDetails() {
        TripSession session = new TripSession();
        session.setId(301L);
        session.setTrip(testTrip);
        session.setStartTime(OffsetDateTime.now().plusDays(2));
        session.setEndTime(OffsetDateTime.now().plusDays(2).plusMinutes(150));
        session.setActive(true);

        when(tripRepository.findWithDetailsById(100L)).thenReturn(Optional.of(testTrip));
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.MAIN))
                .thenReturn(Optional.of(testMainLocation));
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.DEPARTURE))
                .thenReturn(Optional.of(testDepartureLocation));
        when(tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(100L))
                .thenReturn(List.of(session));


        when(reservationRepository.sumBookedSeats(301L)).thenReturn(3L);
        when(locationRepository.findByTripIdAndTypeOrderByNameAsc(100L, LocationType.LANDMARK))
                .thenReturn(Collections.emptyList());

        TripDetailsResponse response = tripService.getTripById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getAdditionalServices()).hasSize(1);
        assertThat(response.getAdditionalServices().get(0).getName()).isEqualTo("Audio guide");
        assertThat(response.getScheduledSessions()).hasSize(1);

        assertThat(response.getScheduledSessions().get(0).getAvailableSeats()).isEqualTo(15L);
    }

    @Test
    @DisplayName("getTripById baca ResourceNotFoundException kada izlet ne postoji")
    void getTripById_whenTripNotFound_throwsException() {
        when(tripRepository.findWithDetailsById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getTripById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trip with id 999 not found");
    }

    @Test
    @DisplayName("getTripById baca ResourceNotFoundException kada je izlet deaktiviran (soft-deleted)")
    void getTripById_whenTripInactive_throwsException() {
        testTrip.setActive(false);
        when(tripRepository.findWithDetailsById(100L)).thenReturn(Optional.of(testTrip));

        assertThatThrownBy(() -> tripService.getTripById(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("is not active");
    }

    @Test
    @DisplayName("createTrip uspješno sprema izlet i njegove lokacije")
    void createTrip_withValidData_savesAndReturnsTrip() {
        CreateTripRequest request = CreateTripRequest.builder()
                .title("Plitvice Lakes Tour")
                .description("Explore national park.")
                .categoryId(1L)
                .durationMinutes(300)
                .price(new BigDecimal("65.00"))
                .maxParticipants(12)
                .requiredGuideCount(2)
                .location("Plitvice Lakes National Park")
                .departurePoint("Entrance 1")
                .build();

        Trip savedTrip = new Trip();
        savedTrip.setId(101L);
        savedTrip.setName("Plitvice Lakes Tour");
        savedTrip.setDescription("Explore national park.");
        savedTrip.setCategory(testCategory);
        savedTrip.setDurationMinutes(300);
        savedTrip.setPrice(new BigDecimal("65.00"));
        savedTrip.setMaxParticipants(12);
        savedTrip.setRequiredGuides(2);
        savedTrip.setActive(true);
        savedTrip.setAdditionalServices(Collections.emptySet());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(tripRepository.save(any(Trip.class))).thenReturn(savedTrip);
        when(tripRepository.findWithDetailsById(101L)).thenReturn(Optional.of(savedTrip));

        TripDetailsResponse result = tripService.createTrip(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getTitle()).isEqualTo("Plitvice Lakes Tour");
        verify(tripRepository).save(any(Trip.class));


        verify(locationRepository, org.mockito.Mockito.atLeastOnce()).save(any(Location.class));
    }

    @Test
    @DisplayName("createTrip baca BadRequestException kada nedostaje naziv izleta")
    void createTrip_missingTitle_throwsException() {
        CreateTripRequest request = CreateTripRequest.builder()
                .description("No title provided")
                .price(new BigDecimal("10.00"))
                .maxParticipants(5)
                .build();

        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("name/title is required");

        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip baca BadRequestException kada kategorija s danim ID-jem ne postoji")
    void createTrip_unknownCategoryId_throwsException() {
        CreateTripRequest request = CreateTripRequest.builder()
                .title("Tour")
                .description("Description")
                .categoryId(99L)
                .price(new BigDecimal("10.00"))
                .maxParticipants(5)
                .build();

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("99");

        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip ne stvara novu kategoriju kada naziv kategorije ne postoji")
    void createTrip_unknownCategoryName_throwsExceptionAndDoesNotCreateCategory() {
        CreateTripRequest request = CreateTripRequest.builder()
                .title("Tour")
                .description("Description")
                .activityCategory("Paragliding")
                .price(new BigDecimal("10.00"))
                .maxParticipants(5)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Paragliding")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Paragliding");

        verify(categoryRepository, never()).save(any());
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip baca BadRequestException kada neka dodatna usluga ne postoji")
    void createTrip_unknownAdditionalServiceId_throwsException() {
        CreateTripRequest request = CreateTripRequest.builder()
                .title("Tour")
                .description("Description")
                .categoryId(1L)
                .price(new BigDecimal("10.00"))
                .maxParticipants(5)
                .additionalServiceIds(List.of(10L, 77L))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(additionalServiceRepository.findAllById(any())).thenReturn(List.of(testService));

        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("[77]");

        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTrip ažurira podatke izleta")
    void updateTrip_whenValid_updatesAndReturnsTrip() {
        UpdateTripRequest updateRequest = UpdateTripRequest.builder()
                .title("Updated Zagreb Tour")
                .price(new BigDecimal("30.00"))
                .maxParticipants(20)
                .locationName("Zagreb Old City")
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(testTrip)).thenReturn(testTrip);
        when(locationRepository.findFirstByTripIdAndType(100L, LocationType.MAIN))
                .thenReturn(Optional.of(testMainLocation));
        when(tripRepository.findWithDetailsById(100L)).thenReturn(Optional.of(testTrip));

        TripDetailsResponse result = tripService.updateTrip(100L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(testTrip.getName()).isEqualTo("Updated Zagreb Tour");
        assertThat(testTrip.getPrice()).isEqualByComparingTo("30.00");
        assertThat(testTrip.getMaxParticipants()).isEqualTo(20);
        verify(tripRepository).save(testTrip);
    }

    @Test
    @DisplayName("deleteTrip provodi meko brisanje na izletu i njegovim terminima")
    void deleteTrip_softDeletesTripAndSessions() {
        TripSession session = new TripSession();
        session.setId(401L);
        session.setTrip(testTrip);
        session.setActive(true);

        when(tripRepository.findById(100L)).thenReturn(Optional.of(testTrip));
        when(tripSessionRepository.findByTripIdAndActiveTrueOrderByStartTimeAsc(100L))
                .thenReturn(List.of(session));

        tripService.deleteTrip(100L);


        assertThat(testTrip.isActive()).isFalse();
        assertThat(session.isActive()).isFalse();
        verify(tripRepository).save(testTrip);
        verify(tripSessionRepository).save(session);
    }

    @Test
    @DisplayName("addSessionToTrip uspješno dodaje termin s automatski izračunatim završetkom")
    void addSessionToTrip_whenValid_createsSession() {
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(5);
        CreateTripSessionRequest request = CreateTripSessionRequest.builder()
                .startTime(startTime)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(testTrip));
        when(tripSessionRepository.existsByTripIdAndStartTimeAndActiveTrue(100L, startTime)).thenReturn(false);

        TripSession savedSession = new TripSession();
        savedSession.setId(501L);
        savedSession.setTrip(testTrip);
        savedSession.schedule(startTime);
        savedSession.setActive(true);

        when(tripSessionRepository.save(any(TripSession.class))).thenReturn(savedSession);

        TripSessionResponse response = tripService.addSessionToTrip(100L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(501L);
        assertThat(response.getAvailableSeats()).isEqualTo(18L);
        verify(tripSessionRepository).save(any(TripSession.class));
    }

    @Test
    @DisplayName("addSessionToTrip baca BadRequestException ako je termin u prošlosti")
    void addSessionToTrip_pastStartTime_throwsException() {
        OffsetDateTime pastTime = OffsetDateTime.now().minusHours(2);
        CreateTripSessionRequest request = CreateTripSessionRequest.builder()
                .startTime(pastTime)
                .build();

        when(tripRepository.findById(100L)).thenReturn(Optional.of(testTrip));

        assertThatThrownBy(() -> tripService.addSessionToTrip(100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start time must be in the future");

        verify(tripSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelSession deaktivira termin")
    void cancelSession_deactivatesSession() {
        TripSession session = new TripSession();
        session.setId(502L);
        session.setTrip(testTrip);
        session.setActive(true);

        when(tripSessionRepository.findById(502L)).thenReturn(Optional.of(session));

        tripService.cancelSession(100L, 502L);

        assertThat(session.isActive()).isFalse();
        verify(tripSessionRepository).save(session);
    }

    @Test
    @DisplayName("calculatePrice točno računa osnovnu cijenu i dodatne usluge za više osoba")
    void calculatePrice_correctlyCalculatesBaseAndAddons() {
        when(tripRepository.findById(100L)).thenReturn(Optional.of(testTrip));
        when(additionalServiceRepository.findAllById(List.of(10L))).thenReturn(List.of(testService));


        PriceCalculationResponse calc = tripService.calculatePrice(100L, 3, List.of(10L));

        assertThat(calc).isNotNull();
        assertThat(calc.getNumberOfParticipants()).isEqualTo(3);
        assertThat(calc.getPricePerPerson()).isEqualByComparingTo("25.00");
        assertThat(calc.getBaseTotal()).isEqualByComparingTo("75.00");
        assertThat(calc.getAddonsTotalPerPerson()).isEqualByComparingTo("8.00");
        assertThat(calc.getAddonsTotal()).isEqualByComparingTo("24.00");
        assertThat(calc.getGrandTotal()).isEqualByComparingTo("99.00");
        assertThat(calc.getSelectedServices()).hasSize(1);
    }
}
