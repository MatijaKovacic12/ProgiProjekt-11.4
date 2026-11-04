package touristAgency.unitTests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import touristAgency.dto.GuideScheduleResponse;
import touristAgency.dto.GuideSessionResponse;
import touristAgency.dto.SessionGuidesResponse;
import touristAgency.exceptions.BadRequestException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.mappers.GuideMapper;
import touristAgency.models.AppUser;
import touristAgency.models.Location;
import touristAgency.models.LocationType;
import touristAgency.models.Role;
import touristAgency.models.Trip;
import touristAgency.models.TripSession;
import touristAgency.models.TripSessionGuide;
import touristAgency.models.TripSessionGuideId;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.LocationRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.TripSessionGuideRepository;
import touristAgency.repositories.TripSessionRepository;
import touristAgency.services.GuideService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

    @Mock
    private TripSessionGuideRepository tripSessionGuideRepository;

    @Mock
    private TripSessionRepository tripSessionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Spy
    private GuideMapper guideMapper = new GuideMapper();

    @InjectMocks
    private GuideService guideService;

    private Trip trip;
    private TripSession session;
    private AppUser guide;

    @BeforeEach
    void setUp() {
        trip = new Trip();
        trip.setId(1L);
        trip.setName("Zagreb Walking Tour");
        trip.setDurationMinutes(120);
        trip.setPrice(new BigDecimal("25.00"));
        trip.setMaxParticipants(10);
        trip.setRequiredGuides(2);
        trip.setActive(true);

        session = new TripSession();
        session.setId(10L);
        session.setTrip(trip);
        session.schedule(OffsetDateTime.now().plusDays(3));
        session.setActive(true);

        guide = new AppUser();
        guide.setId(5L);
        guide.setFirstName("Luka");
        guide.setLastName("Marić");
        guide.setEmail("luka@test.hr");
        guide.setRole(Role.GUIDE);
    }

    @Test
    @DisplayName("getSessionsForGuide vraća termine vodiča s mjestom polaska i brojem prijavljenih")
    void getSessionsForGuide_returnsSessionsWithDepartureAndBookedPeople() {
        Location departure = new Location();
        departure.setName("Ban Jelačić Square");

        when(tripSessionGuideRepository.findActiveAssignmentsForGuide(eq(5L), any(OffsetDateTime.class)))
                .thenReturn(List.of(new TripSessionGuide(session, guide)));
        when(locationRepository.findFirstByTripIdAndType(1L, LocationType.DEPARTURE)).thenReturn(Optional.of(departure));
        when(reservationRepository.sumBookedSeats(10L)).thenReturn(4L);

        List<GuideSessionResponse> result = guideService.getSessionsForGuide(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionId()).isEqualTo(10L);
        assertThat(result.get(0).getTripName()).isEqualTo("Zagreb Walking Tour");
        assertThat(result.get(0).getDeparturePoint()).isEqualTo("Ban Jelačić Square");
        assertThat(result.get(0).getBookedPeople()).isEqualTo(4L);
    }

    @Test
    @DisplayName("getGuideSchedules vraća sve vodiče, i one bez dodjela, bez otkazanih termina")
    void getGuideSchedules_includesGuidesWithoutAssignmentsAndSkipsCancelledSessions() {
        AppUser freeGuide = new AppUser();
        freeGuide.setId(6L);
        freeGuide.setRole(Role.GUIDE);

        TripSession cancelled = new TripSession();
        cancelled.setId(11L);
        cancelled.setTrip(trip);
        cancelled.schedule(OffsetDateTime.now().plusDays(5));
        cancelled.setActive(false);

        when(tripSessionGuideRepository.findUpcomingAssignments(any(OffsetDateTime.class)))
                .thenReturn(List.of(new TripSessionGuide(session, guide), new TripSessionGuide(cancelled, guide)));
        when(appUserRepository.findByRoleOrderByLastNameAscFirstNameAsc(Role.GUIDE)).thenReturn(List.of(guide, freeGuide));
        when(locationRepository.findFirstByTripIdAndType(1L, LocationType.DEPARTURE)).thenReturn(Optional.empty());

        List<GuideScheduleResponse> result = guideService.getGuideSchedules();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUpcomingSessions()).extracting(GuideSessionResponse::getSessionId).containsExactly(10L);
        assertThat(result.get(1).getUpcomingSessions()).isEmpty();
    }

    @Test
    @DisplayName("getUpcomingSessionsWithGuides označava termin bez dovoljno vodiča")
    void getUpcomingSessionsWithGuides_marksUnderstaffedSession() {
        when(tripSessionGuideRepository.findUpcomingAssignments(any(OffsetDateTime.class)))
                .thenReturn(List.of(new TripSessionGuide(session, guide)));
        when(tripSessionRepository.findUpcomingActive(any(OffsetDateTime.class))).thenReturn(List.of(session));

        List<SessionGuidesResponse> result = guideService.getUpcomingSessionsWithGuides();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedGuides()).hasSize(1);
        assertThat(result.get(0).getRequiredGuides()).isEqualTo(2);
        assertThat(result.get(0).isFullyStaffed()).isFalse();
    }

    @Test
    @DisplayName("assignGuide sprema dodjelu kad nema preklapanja")
    void assignGuide_whenNoOverlap_savesAssignment() {
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(guide));
        when(tripSessionGuideRepository.existsById(new TripSessionGuideId(10L, 5L))).thenReturn(false);
        when(tripSessionGuideRepository.hasOverlap(5L, session.getStartTime(), session.getEndTime())).thenReturn(false);
        when(tripSessionGuideRepository.findByTripSessionId(10L)).thenReturn(List.of(new TripSessionGuide(session, guide)));

        SessionGuidesResponse result = guideService.assignGuide(10L, 5L);

        verify(tripSessionGuideRepository).save(any(TripSessionGuide.class));
        assertThat(result.getAssignedGuides()).extracting("id").containsExactly(5L);
    }

    @Test
    @DisplayName("assignGuide odbija vodiča koji je već na preklapajućem terminu (FR-16)")
    void assignGuide_whenOverlap_throwsBadRequest() {
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(guide));
        when(tripSessionGuideRepository.existsById(new TripSessionGuideId(10L, 5L))).thenReturn(false);
        when(tripSessionGuideRepository.hasOverlap(5L, session.getStartTime(), session.getEndTime())).thenReturn(true);

        assertThatThrownBy(() -> guideService.assignGuide(10L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlapping");
        verify(tripSessionGuideRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignGuide odbija korisnika koji nema ulogu vodič")
    void assignGuide_whenUserIsNotGuide_throwsBadRequest() {
        guide.setRole(Role.USER);
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(guide));

        assertThatThrownBy(() -> guideService.assignGuide(10L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a guide");
    }

    @Test
    @DisplayName("assignGuide odbija dvostruku dodjelu istom terminu")
    void assignGuide_whenAlreadyAssigned_throwsBadRequest() {
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));
        when(appUserRepository.findById(5L)).thenReturn(Optional.of(guide));
        when(tripSessionGuideRepository.existsById(new TripSessionGuideId(10L, 5L))).thenReturn(true);

        assertThatThrownBy(() -> guideService.assignGuide(10L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    @DisplayName("assignGuide odbija otkazani termin")
    void assignGuide_whenSessionCancelled_throwsBadRequest() {
        session.setActive(false);
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> guideService.assignGuide(10L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("assignGuide odbija termin koji je već započeo")
    void assignGuide_whenSessionStarted_throwsBadRequest() {
        session.schedule(OffsetDateTime.now().minusMinutes(30));
        when(tripSessionRepository.findWithTripById(10L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> guideService.assignGuide(10L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already started");
    }

    @Test
    @DisplayName("assignGuide baca ResourceNotFoundException za nepostojeći termin")
    void assignGuide_whenSessionMissing_throwsNotFound() {
        when(tripSessionRepository.findWithTripById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guideService.assignGuide(99L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("unassignGuide briše postojeću dodjelu")
    void unassignGuide_whenAssigned_deletes() {
        TripSessionGuideId id = new TripSessionGuideId(10L, 5L);
        when(tripSessionGuideRepository.existsById(id)).thenReturn(true);

        guideService.unassignGuide(10L, 5L);

        verify(tripSessionGuideRepository).deleteById(id);
    }

    @Test
    @DisplayName("unassignGuide baca ResourceNotFoundException kad dodjela ne postoji")
    void unassignGuide_whenNotAssigned_throwsNotFound() {
        when(tripSessionGuideRepository.existsById(new TripSessionGuideId(10L, 5L))).thenReturn(false);

        assertThatThrownBy(() -> guideService.unassignGuide(10L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(tripSessionGuideRepository, never()).deleteById(any());
    }
}
