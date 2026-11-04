package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import touristAgency.controllers.AdminReservationController;
import touristAgency.exceptions.BusinessConflictException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.models.*;
import touristAgency.services.ReservationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsOk() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .param("page", "-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .param("size", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenSizeExceedsMaximum() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .param("size", "101")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesTripIdToService() throws Exception {
        when(reservationService.searchReservations(
                eq(1L),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("tripId", "1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                eq(1L),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesSessionIdToService() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                eq(2L),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("sessionId", "2")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                eq(2L),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesStatusToService() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                eq(ReservationStatus.CONFIRMED),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                isNull(),
                eq(ReservationStatus.CONFIRMED),
                eq(""),
                isNull(),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesSearchTermToService() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq("Ana"),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("q", "Ana")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq("Ana"),
                isNull(),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesCreatedDateToService() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 15);

        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                eq(date),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("createdDate", "2026-10-15")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                eq(date),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesPaginationToService() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());
        mockMvc.perform(get("/api/admin/reservations")
                        .param("page", "2")
                        .param("size", "5")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                argThat(pageable ->
                        pageable.getPageNumber() == 2 &&
                                pageable.getPageSize() == 5)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", "INVALID")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsEmptyPageWhenNoReservationsFound() throws Exception {
        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .param("createdDate", "15-10-2026")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_passesMultipleFiltersToService() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 15);

        when(reservationService.searchReservations(
                eq(1L),
                eq(2L),
                eq(ReservationStatus.CONFIRMED),
                eq("Ana"),
                eq(date),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("tripId", "1")
                        .param("sessionId", "2")
                        .param("status", "CONFIRMED")
                        .param("q", "Ana")
                        .param("createdDate", "2026-10-15")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                eq(1L),
                eq(2L),
                eq(ReservationStatus.CONFIRMED),
                eq("Ana"),
                eq(date),
                any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_mapsReservationToResponse() throws Exception {
        OffsetDateTime startTime =
                OffsetDateTime.parse("2026-11-01T10:00:00+01:00");
        OffsetDateTime createdAt =
                OffsetDateTime.parse("2026-10-15T12:00:00+02:00");

        AppUser customer = new AppUser();
        customer.setFirstName("Ana");
        customer.setLastName("Horvat");
        customer.setEmail("ana@test.hr");

        Trip trip = new Trip();
        trip.setId(10L);
        trip.setName("Planinarenje");

        TripSession session = new TripSession();
        session.setId(20L);
        session.setTrip(trip);
        session.setStartTime(startTime);

        AdditionalService additionalService = new AdditionalService();
        additionalService.setName("Ručak");
        additionalService.setPrice(new BigDecimal("15.00"));

        Reservation reservation = new Reservation();
        reservation.setId(30L);
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setNumberOfPeople(2);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(createdAt);
        reservation.setTotalPrice(new BigDecimal("150.00"));
        reservation.addAdditionalService(additionalService, 1);

        when(reservationService.searchReservations(
                isNull(),
                isNull(),
                isNull(),
                eq(""),
                isNull(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(reservation)));

        mockMvc.perform(get("/api/admin/reservations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(30))
                .andExpect(jsonPath("$.content[0].tripId").value(10))
                .andExpect(jsonPath("$.content[0].tourTitle").value("Planinarenje"))
                .andExpect(jsonPath("$.content[0].sessionId").value(20))
                .andExpect(jsonPath("$.content[0].startTime").value("2026-11-01T10:00:00+01:00"))
                .andExpect(jsonPath("$.content[0].userName").value("Ana Horvat"))
                .andExpect(jsonPath("$.content[0].userEmail").value("ana@test.hr"))
                .andExpect(jsonPath("$.content[0].participants").value(2))
                .andExpect(jsonPath("$.content[0].addons[0]").value("Ručak"))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[0].createdAt").value("2026-10-15T12:00:00+02:00"))
                .andExpect(jsonPath("$.content[0].totalPrice").value(150.00));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_trimsSearchTerm() throws Exception {
        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq("Ana"), isNull(), any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations")
                        .param("q", "  Ana  ")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reservationService).searchReservations(
                isNull(), isNull(), isNull(), eq("Ana"), isNull(), any(Pageable.class)
        );
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_usesDefaultPagination() throws Exception {
        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations").with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(reservationService).searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_sortsByCreatedAtDescending() throws Exception {
        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/reservations").with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(reservationService).searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals("createdAt", pageable.getSort().getOrderFor("createdAt").getProperty());
        assertEquals(Sort.Direction.DESC,
                pageable.getSort().getOrderFor("createdAt").getDirection());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsMultipleReservations() throws Exception {
        AppUser customer = new AppUser();
        customer.setFirstName("Ana");
        customer.setLastName("Horvat");
        customer.setEmail("ana@test.hr");

        Trip trip = new Trip();
        trip.setName("Planinarenje");

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setStartTime(OffsetDateTime.parse("2026-11-01T10:00:00+01:00"));

        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setCustomer(customer);
        reservation1.setTripSession(session);
        reservation1.setNumberOfPeople(2);
        reservation1.setStatus(ReservationStatus.CONFIRMED);
        reservation1.setCreatedAt(OffsetDateTime.parse("2026-10-15T12:00:00+02:00"));
        reservation1.setTotalPrice(new BigDecimal("100.00"));

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setCustomer(customer);
        reservation2.setTripSession(session);
        reservation2.setNumberOfPeople(3);
        reservation2.setStatus(ReservationStatus.CONFIRMED);
        reservation2.setCreatedAt(OffsetDateTime.parse("2026-10-16T12:00:00+02:00"));
        reservation2.setTotalPrice(new BigDecimal("150.00"));

        Page<Reservation> reservations = new PageImpl<>(
                List.of(reservation1, reservation2)
        );

        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                any(Pageable.class)
        )).thenReturn(reservations);

        mockMvc.perform(get("/api/admin/reservations").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsBadRequestWhenServiceThrowsIllegalArgumentException()
            throws Exception {

        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                any(Pageable.class)
        )).thenThrow(new IllegalArgumentException("Neispravan parametar"));

        mockMvc.perform(get("/api/admin/reservations").with(csrf()))
                .andExpect(status().isBadRequest());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservations_returnsConflictWhenServiceThrowsIllegalStateException()
            throws Exception {

        when(reservationService.searchReservations(
                isNull(), isNull(), isNull(), eq(""), isNull(),
                any(Pageable.class)
        )).thenThrow(new IllegalStateException("Nedozvoljeno stanje"));

        mockMvc.perform(get("/api/admin/reservations").with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelReservation_returnsUpdatedReservation() throws Exception {
        AppUser customer = new AppUser();
        customer.setFirstName("Ana");
        customer.setLastName("Horvat");
        customer.setEmail("ana@test.hr");

        Trip trip = new Trip();
        trip.setId(10L);
        trip.setName("Planinarenje");

        TripSession session = new TripSession();
        session.setId(20L);
        session.setTrip(trip);

        Reservation reservation = new Reservation();
        reservation.setId(30L);
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setNumberOfPeople(2);
        reservation.setTotalPrice(new BigDecimal("150.00"));
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationService.cancelReservationAsAdmin(30L)).thenReturn(reservation);

        mockMvc.perform(post("/api/admin/reservations/30/cancel").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.userName").value("Ana Horvat"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(reservationService).cancelReservationAsAdmin(30L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelReservation_returnsNotFound_WhenReservationDoesNotExist() throws Exception {
        when(reservationService.cancelReservationAsAdmin(99L))
                .thenThrow(new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Rezervacija ne postoji."));

        mockMvc.perform(post("/api/admin/reservations/99/cancel").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelReservation_returnsConflict_WhenReservationIsNotCancellable() throws Exception {
        when(reservationService.cancelReservationAsAdmin(30L))
                .thenThrow(new BusinessConflictException("RESERVATION_NOT_CANCELLABLE", "Nije moguće otkazati."));

        mockMvc.perform(post("/api/admin/reservations/30/cancel").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_CANCELLABLE"));
    }
}