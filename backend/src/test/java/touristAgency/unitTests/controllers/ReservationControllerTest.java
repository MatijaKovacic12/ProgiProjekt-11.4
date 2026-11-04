package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import touristAgency.controllers.ReservationController;
import touristAgency.exceptions.BusinessConflictException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.services.CurrentUserService;
import touristAgency.models.AdditionalService;
import touristAgency.models.AppUser;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;
import touristAgency.models.Trip;
import touristAgency.models.TripSession;
import touristAgency.services.ReservationService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void loggedInUserIsOne() {
        when(currentUserService.requireUserId()).thenReturn(1L);
    }

    @Test
    @WithMockUser
    void getMyReservations_ShouldReturnUnauthorized_WhenUserIsNotKnown() throws Exception {
        when(currentUserService.requireUserId())
                .thenThrow(new UnauthorizedException("Korisnik nije prijavljen."));

        mockMvc.perform(get("/api/reservations/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        verifyNoInteractions(reservationService);
    }

    @Test
    @WithMockUser
    void createReservation_ShouldUseIdOfLoggedInUser() throws Exception {
        when(currentUserService.requireUserId()).thenReturn(42L);
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenReturn(sampleReservation(ReservationStatus.CONFIRMED));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isCreated());

        verify(reservationService).book(eq(42L), eq(1L), eq(2), anyList());
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnBadRequest_WhenParticipantsAreZero() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 0,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser
    void createReservation_ShouldReturnBadRequest_WhenSessionIdIsNull() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": null,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser
    void createReservation_ShouldReturnBadRequest_WhenParticipantsAreNegative() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": -2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser
    void createReservation_ShouldReturnCreated_WhenReservationIsSuccessful() throws Exception {
        Trip trip = new Trip();
        trip.setName("Testni izlet");

        TripSession tripSession = new TripSession();
        tripSession.setId(1L);
        tripSession.setTrip(trip);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setTripSession(tripSession);
        reservation.setNumberOfPeople(2);
        reservation.setTotalPrice(new BigDecimal("100.00"));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/reservations/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.tourTitle").value("Testni izlet"))
                .andExpect(jsonPath("$.participants").value(2))
                .andExpect(jsonPath("$.totalPrice").value(100.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService).book(eq(1L), eq(1L), eq(2), anyList());
    }
    @Test
    @WithMockUser
    void createReservation_ShouldReturnConflict_WhenServiceThrowsIllegalStateException() throws Exception {
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenThrow(new BusinessConflictException("NOT_ENOUGH_SEATS", "Nema dovoljno slobodnih mjesta"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("NOT_ENOUGH_SEATS"))
                .andExpect(jsonPath("$.message").value("Nema dovoljno slobodnih mjesta"));

        verify(reservationService).book(eq(1L), eq(1L), eq(2), anyList());
    }
    @Test
    @WithMockUser
    void createReservation_ShouldReturnBadRequest_WhenServiceThrowsIllegalArgumentException() throws Exception {
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenThrow(new IllegalArgumentException("Neispravni podaci rezervacije"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Neispravni podaci rezervacije"));

        verify(reservationService).book(eq(1L), eq(1L), eq(2), anyList());
    }
    @Test
    @WithMockUser
    void getMyReservations_ShouldReturnOk() throws Exception {
        when(reservationService.getMyReservations(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/reservations/mine"))
                .andExpect(status().isOk());

        verify(reservationService).getMyReservations(1L);
    }
    @Test
    @WithMockUser
    void getReservationDetails_ShouldReturnOk() throws Exception {
        Trip trip = new Trip();
        trip.setName("Testni izlet");

        TripSession tripSession = new TripSession();
        tripSession.setId(1L);
        tripSession.setTrip(trip);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setTripSession(tripSession);

        when(reservationService.getReservationDetails(1L, 1L))
                .thenReturn(reservation);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.tourTitle").value("Testni izlet"));

        verify(reservationService).getReservationDetails(1L, 1L);
    }
    @Test
    @WithMockUser
    void getReservationDetails_ShouldReturnNotFound_WhenReservationDoesNotExist() throws Exception {
        when(reservationService.getReservationDetails(1L, 1L))
                .thenThrow(new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Rezervacija nije pronađena"));

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));

        verify(reservationService).getReservationDetails(1L, 1L);
    }
    @Test
    @WithMockUser
    void cancelReservation_ShouldReturnOk_WhenCancellationIsSuccessful() throws Exception {
        Reservation reservation = sampleReservation(ReservationStatus.CANCELLED);

        when(reservationService.cancelReservation(1L, 1L))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/reservations/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(reservationService).cancelReservation(1L, 1L);
    }
    @Test
    @WithMockUser
    void cancelReservation_ShouldReturnNotFound_WhenReservationDoesNotExist() throws Exception {
        when(reservationService.cancelReservation(1L, 1L))
                .thenThrow(new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Rezervacija nije pronađena"));

        mockMvc.perform(post("/api/reservations/1/cancel")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));

        verify(reservationService).cancelReservation(1L, 1L);
    }
    @Test
    @WithMockUser
    void cancelReservation_ShouldReturnConflict_WhenCancellationIsNotPossible() throws Exception {
        when(reservationService.cancelReservation(1L, 1L))
                .thenThrow(new BusinessConflictException("RESERVATION_NOT_CANCELLABLE", "Rezervaciju nije moguće otkazati"));

        mockMvc.perform(post("/api/reservations/1/cancel")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_CANCELLABLE"));

        verify(reservationService).cancelReservation(1L, 1L);
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnFieldErrors_WhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "participants": 0
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.sessionId").exists())
                .andExpect(jsonPath("$.fieldErrors.participants").exists());

        verifyNoInteractions(reservationService);
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnBadRequest_WhenBodyIsNotValidJson() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{ ovo nije json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(reservationService);
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnNotFound_WhenSessionDoesNotExist() throws Exception {
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenThrow(new ResourceNotFoundException("SESSION_NOT_FOUND", "Termin ne postoji."));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 99,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnConflict_WhenSessionIsNotBookable() throws Exception {
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenThrow(new BusinessConflictException("SESSION_NOT_BOOKABLE", "Termin više nije dostupan."));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_NOT_BOOKABLE"));
    }

    @Test
    @WithMockUser
    void createReservation_ShouldReturnGenericConflict_WhenServiceThrowsPlainIllegalState() throws Exception {
        when(reservationService.book(anyLong(), anyLong(), anyInt(), anyList()))
                .thenThrow(new IllegalStateException("Nedopušteno stanje"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                    {
                        "sessionId": 1,
                        "participants": 2,
                        "addonIds": []
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @WithMockUser
    void getMyReservations_ShouldReturnMappedList() throws Exception {
        when(reservationService.getMyReservations(1L))
                .thenReturn(java.util.List.of(sampleReservation(ReservationStatus.CONFIRMED)));

        mockMvc.perform(get("/api/reservations/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tourTitle").value("Testni izlet"))
                .andExpect(jsonPath("$[0].participants").value(2))
                .andExpect(jsonPath("$[0].addons[0]").value("Ručak"))
                .andExpect(jsonPath("$[0].totalPrice").value(100.00))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    private Reservation sampleReservation(ReservationStatus status) {
        AdditionalService lunch = new AdditionalService();
        lunch.setId(5L);
        lunch.setName("Ručak");
        lunch.setPrice(new BigDecimal("10.00"));

        Trip trip = new Trip();
        trip.setId(3L);
        trip.setName("Testni izlet");

        TripSession tripSession = new TripSession();
        tripSession.setId(1L);
        tripSession.setTrip(trip);
        tripSession.setStartTime(OffsetDateTime.now().plusDays(3));

        AppUser customer = new AppUser();
        customer.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomer(customer);
        reservation.setTripSession(tripSession);
        reservation.setNumberOfPeople(2);
        reservation.setTotalPrice(new BigDecimal("100.00"));
        reservation.setStatus(status);
        reservation.addAdditionalService(lunch, 2);
        return reservation;
    }
}