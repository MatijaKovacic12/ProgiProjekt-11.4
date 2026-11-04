package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.ArgumentCaptor;
import touristAgency.events.ReservationCancelledEvent;
import touristAgency.events.ReservationConfirmedEvent;
import touristAgency.exceptions.BusinessConflictException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.models.*;
import touristAgency.repositories.*;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;
import touristAgency.services.ReservationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import touristAgency.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservations;

    @Mock
    private TripSessionRepository tripSessions;

    @Mock
    private AdditionalServiceRepository additionalServices;

    @Mock
    private AppUserRepository users;

    @Mock
    private ReservationAdditionalServiceRepository reservationAdditionalServices;

    @Mock
    private ApplicationEventPublisher events;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void book_ShouldThrowException_WhenCapacityIsExceeded() {

        // Arrange - priprema podataka
        AppUser customer = new AppUser();

        Trip trip = new Trip();
        trip.setActive(true);
        trip.setMaxParticipants(15);
        trip.setPrice(new BigDecimal("20.00"));

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setActive(true);
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        when(users.findById(1L))
                .thenReturn(Optional.of(customer));

        when(tripSessions.findByIdForUpdate(7L))
                .thenReturn(Optional.of(session));

        when(reservations.sumBookedSeats(7L))
                .thenReturn(10L);

        // Act + Assert - poziv metode i provjera rezultata
        assertThrows(IllegalStateException.class, () ->
                reservationService.book(
                        1L,
                        7L,
                        6,
                        List.of()
                )
        );

        verify(reservations, never()).save(any());
    }
    @Test
    void getMyReservations_ShouldReturnReservations_WhenCustomerExists() {
        // Arrange
        AppUser customer = new AppUser();
        Reservation reservation = new Reservation();

        List<Reservation> expected = List.of(reservation);

        when(users.existsById(1L)).thenReturn(true);
        when(reservations.findByCustomerIdOrderByCreatedAtDesc(1L))
                .thenReturn(expected);

        // Act
        List<Reservation> result = reservationService.getMyReservations(1L);

        // Assert
        assertEquals(expected, result);
        verify(reservations).findByCustomerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getMyReservations_ShouldThrowException_WhenCustomerDoesNotExist() {
        // Arrange
        when(users.existsById(1L)).thenReturn(false);

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.getMyReservations(1L)
        );

        verify(reservations, never())
                .findByCustomerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void book_ShouldCreateReservation_WhenDataIsValid() {
        // Arrange
        AppUser customer = new AppUser();

        Trip trip = new Trip();
        trip.setActive(true);
        trip.setMaxParticipants(15);
        trip.setPrice(new BigDecimal("20.00"));

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setActive(true);
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        when(users.findById(1L)).thenReturn(Optional.of(customer));
        when(tripSessions.findByIdForUpdate(7L)).thenReturn(Optional.of(session));
        when(reservations.sumBookedSeats(7L)).thenReturn(5L);

        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Reservation result = reservationService.book(1L, 7L, 2, List.of());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getNumberOfPeople());
        assertEquals(new BigDecimal("40.00"), result.getTotalPrice());

        verify(reservations).save(any(Reservation.class));
    }
    @Test
    void getReservationDetails_ShouldReturnReservation_WhenCustomerIsOwner() {
        AppUser customer = new AppUser();
        customer.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationDetails(21L, 1L);

        assertNotNull(result);
        assertEquals(reservation, result);

        verify(reservations).findDetailsById(21L);
    }
    @Test
    void getReservationDetails_ShouldThrowException_WhenReservationDoesNotExist() {
        // Arrange
        when(reservations.findDetailsById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.getReservationDetails(999L, 1L)
        );
    }
    @Test
    void getReservationDetails_ShouldThrowException_WhenCustomerIsNotOwner() {
        // Arrange
        Reservation reservation = new Reservation();
        AppUser customer = new AppUser();
        customer.setId(2L);
        reservation.setCustomer(customer);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.getReservationDetails(21L, 1L)
        );
    }
    @Test
    void cancelReservation_ShouldCancelReservation_WhenDataIsValid() {
        // Arrange
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        when(reservations.save(reservation))
                .thenReturn(reservation);

        // Act
        Reservation result = reservationService.cancelReservation(21L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());

        verify(reservations).save(reservation);
    }
    @Test
    void cancelReservation_ShouldThrowException_WhenReservationDoesNotExist() {
        // Arrange
        when(reservations.findDetailsById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservation(999L, 1L)
        );

        verify(reservations, never()).save(any(Reservation.class));
    }
    @Test
    void cancelReservation_ShouldThrowException_WhenCustomerIsNotOwner() {
        // Arrange
        AppUser customer = new AppUser();
        customer.setId(2L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservation(21L, 1L)
        );

        verify(reservations, never()).save(any(Reservation.class));
    }
    @Test
    void cancelReservation_ShouldThrowException_WhenReservationIsNotConfirmed() {
        // Arrange
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                reservationService.cancelReservation(21L, 1L)
        );

        verify(reservations, never()).save(any(Reservation.class));
    }
    @Test
    void cancelReservation_ShouldThrowException_WhenTripHasAlreadyStarted() {
        // Arrange
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().minusDays(1));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L))
                .thenReturn(Optional.of(reservation));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                reservationService.cancelReservation(21L, 1L)
        );

        verify(reservations, never()).save(any(Reservation.class));
    }
    @Test
    void searchReservations_ShouldSearchWithoutDate_WhenCreatedDateIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> expected = new PageImpl<>(List.of(new Reservation()));

        when(reservations.searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "Marko", pageable))
                .thenReturn(expected);

        // Act
        Page<Reservation> result = reservationService.searchReservations(
                1L, 7L, ReservationStatus.CONFIRMED,
                "Marko", null, pageable);

        // Assert
        assertEquals(expected, result);

        verify(reservations).searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "Marko", pageable);

        verify(reservations, never()).searchAdmin(
                any(), any(), any(), any(), any(), any());
    }
    @Test
    void searchReservations_ShouldSearchWithDate_WhenCreatedDateIsProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate date = LocalDate.of(2026, 10, 30);

        Page<Reservation> expected =
                new PageImpl<>(List.of(new Reservation()));

        when(reservations.searchAdmin(
                1L, 7L, ReservationStatus.CONFIRMED,
                "Marko", date, pageable))
                .thenReturn(expected);

        // Act
        Page<Reservation> result = reservationService.searchReservations(
                1L, 7L, ReservationStatus.CONFIRMED,
                "Marko", date, pageable);

        // Assert
        assertEquals(expected, result);

        verify(reservations).searchAdmin(
                1L, 7L, ReservationStatus.CONFIRMED,
                "Marko", date, pageable);

        verify(reservations, never()).searchAdminWithoutDate(
                any(), any(), any(), any(), any());
    }
    @Test
    void searchReservations_ShouldTrimSearchTerm_WhenTermContainsSpaces() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> expected = Page.empty();

        when(reservations.searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "Marko", pageable))
                .thenReturn(expected);

        // Act
        Page<Reservation> result = reservationService.searchReservations(
                1L, 7L, ReservationStatus.CONFIRMED,
                "  Marko  ", null, pageable);

        // Assert
        assertEquals(expected, result);

        verify(reservations).searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "Marko", pageable);
    }
    @Test
    void searchReservations_ShouldUseEmptyString_WhenTermIsNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> expected = Page.empty();

        when(reservations.searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "", pageable))
                .thenReturn(expected);

        // Act
        Page<Reservation> result = reservationService.searchReservations(
                1L, 7L, ReservationStatus.CONFIRMED,
                null, null, pageable);

        // Assert
        assertEquals(expected, result);

        verify(reservations).searchAdminWithoutDate(
                1L, 7L, ReservationStatus.CONFIRMED, "", pageable);
    }
    @Test
    void getActiveInProgress_ShouldReturnReservations_WhenCustomerExists() {
        // Arrange
        Reservation reservation = new Reservation();
        List<Reservation> expected = List.of(reservation);

        when(users.existsById(1L)).thenReturn(true);
        when(reservations.findActiveInProgress(
                eq(1L), any(OffsetDateTime.class)))
                .thenReturn(expected);

        // Act
        List<Reservation> result =
                reservationService.getActiveInProgress(1L);

        // Assert
        assertEquals(expected, result);

        verify(reservations).findActiveInProgress(
                eq(1L), any(OffsetDateTime.class));
    }
    @Test
    void getActiveInProgress_ShouldThrowException_WhenCustomerDoesNotExist() {
        // Arrange
        when(users.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.getActiveInProgress(1L)
        );

        verify(reservations, never()).findActiveInProgress(
                anyLong(), any(OffsetDateTime.class));
    }
    @Test
    void getStatisticsSummary_ShouldReturnStatistics_WhenPeriodIsValid() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now().minusDays(30);
        OffsetDateTime dateTo = OffsetDateTime.now();

        StatisticsSummary expected = mock(StatisticsSummary.class);

        when(reservations.getStatisticsSummary(dateFrom, dateTo))
                .thenReturn(expected);

        // Act
        StatisticsSummary result =
                reservationService.getStatisticsSummary(dateFrom, dateTo);

        // Assert
        assertEquals(expected, result);

        verify(reservations).getStatisticsSummary(dateFrom, dateTo);
    }
    @Test
    void getStatisticsSummary_ShouldThrowException_WhenDateFromIsAfterDateTo() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now();
        OffsetDateTime dateTo = dateFrom.minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.getStatisticsSummary(dateFrom, dateTo)
        );

        verify(reservations, never()).getStatisticsSummary(
                any(OffsetDateTime.class), any(OffsetDateTime.class));
    }
    @Test
    void getTripOccupancy_ShouldReturnOccupancy_WhenPeriodIsValid() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now().minusDays(30);
        OffsetDateTime dateTo = OffsetDateTime.now();

        TripOccupancy occupancy = mock(TripOccupancy.class);
        List<TripOccupancy> expected = List.of(occupancy);

        when(reservations.getTripOccupancy(dateFrom, dateTo))
                .thenReturn(expected);

        // Act
        List<TripOccupancy> result =
                reservationService.getTripOccupancy(dateFrom, dateTo);

        // Assert
        assertEquals(expected, result);

        verify(reservations).getTripOccupancy(dateFrom, dateTo);
    }
    @Test
    void getTripOccupancy_ShouldThrowException_WhenDateFromIsAfterDateTo() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now();
        OffsetDateTime dateTo = dateFrom.minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.getTripOccupancy(dateFrom, dateTo)
        );

        verify(reservations, never()).getTripOccupancy(
                any(OffsetDateTime.class), any(OffsetDateTime.class));
    }
    @Test
    void getTop5Categories_ShouldReturnCategories_WhenPeriodIsValid() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now().minusDays(30);
        OffsetDateTime dateTo = OffsetDateTime.now();

        TopItem item = mock(TopItem.class);
        List<TopItem> expected = List.of(item);

        when(reservations.findTop5Categories(dateFrom, dateTo))
                .thenReturn(expected);

        // Act
        List<TopItem> result =
                reservationService.getTop5Categories(dateFrom, dateTo);

        // Assert
        assertEquals(expected, result);

        verify(reservations).findTop5Categories(dateFrom, dateTo);
    }
    @Test
    void getTop5Categories_ShouldThrowException_WhenDateFromIsAfterDateTo() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now();
        OffsetDateTime dateTo = dateFrom.minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.getTop5Categories(dateFrom, dateTo)
        );

        verify(reservations, never()).findTop5Categories(
                any(OffsetDateTime.class), any(OffsetDateTime.class));
    }
    @Test
    void getTop5AdditionalServices_ShouldReturnServices_WhenPeriodIsValid() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now().minusDays(30);
        OffsetDateTime dateTo = OffsetDateTime.now();

        TopItem item = mock(TopItem.class);
        List<TopItem> expected = List.of(item);

        when(reservationAdditionalServices.findTop5AdditionalServices(
                dateFrom, dateTo))
                .thenReturn(expected);

        // Act
        List<TopItem> result =
                reservationService.getTop5AdditionalServices(dateFrom, dateTo);

        // Assert
        assertEquals(expected, result);

        verify(reservationAdditionalServices).findTop5AdditionalServices(
                dateFrom, dateTo);
    }
    @Test
    void getTop5AdditionalServices_ShouldThrowException_WhenDateFromIsAfterDateTo() {
        // Arrange
        OffsetDateTime dateFrom = OffsetDateTime.now();
        OffsetDateTime dateTo = dateFrom.minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.getTop5AdditionalServices(dateFrom, dateTo)
        );

        verify(reservationAdditionalServices, never())
                .findTop5AdditionalServices(
                        any(OffsetDateTime.class),
                        any(OffsetDateTime.class));
    }
    @Test
    void completeFinishedReservations_ShouldCallMarkFinished() {
        // Act
        reservationService.completeFinishedReservations();

        // Assert
        verify(reservations).markFinished(any(OffsetDateTime.class));
    }

    // ---------------------------------------------------------------------
    // book(): dodatni scenariji (termin, kapacitet, cijena, dodatne usluge)
    // ---------------------------------------------------------------------

    private TripSession bookableSession(int maxParticipants, String tripPrice) {
        Trip trip = new Trip();
        trip.setActive(true);
        trip.setMaxParticipants(maxParticipants);
        trip.setPrice(new BigDecimal(tripPrice));

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setActive(true);
        session.setStartTime(OffsetDateTime.now().plusDays(2));
        return session;
    }

    private AdditionalService service(Long id, String price) {
        AdditionalService service = new AdditionalService();
        service.setId(id);
        service.setName("Usluga " + id);
        service.setPrice(new BigDecimal(price));
        return service;
    }

    private void stubCustomerAndSession(TripSession session) {
        when(users.findById(1L)).thenReturn(Optional.of(new AppUser()));
        when(tripSessions.findByIdForUpdate(7L)).thenReturn(Optional.of(session));
    }

    @Test
    void book_ShouldThrowIllegalArgument_WhenPeopleIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                reservationService.book(1L, 7L, 0, List.of()));

        verifyNoInteractions(users, tripSessions, reservations);
    }

    @Test
    void book_ShouldThrowNotFound_WhenCustomerDoesNotExist() {
        when(users.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("USER_NOT_FOUND", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowNotFound_WhenSessionDoesNotExist() {
        when(users.findById(1L)).thenReturn(Optional.of(new AppUser()));
        when(tripSessions.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("SESSION_NOT_FOUND", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowSessionNotBookable_WhenSessionIsInactive() {
        TripSession session = bookableSession(15, "20.00");
        session.setActive(false);
        stubCustomerAndSession(session);

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("SESSION_NOT_BOOKABLE", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowSessionNotBookable_WhenTripIsInactive() {
        TripSession session = bookableSession(15, "20.00");
        session.getTrip().setActive(false);
        stubCustomerAndSession(session);

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("SESSION_NOT_BOOKABLE", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowSessionNotBookable_WhenSessionAlreadyStarted() {
        TripSession session = bookableSession(15, "20.00");
        session.setStartTime(OffsetDateTime.now().minusHours(1));
        stubCustomerAndSession(session);

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("SESSION_NOT_BOOKABLE", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowNotEnoughSeats_WithErrorCode() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(14L);

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        assertEquals("NOT_ENOUGH_SEATS", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldSucceed_WhenBookingExactlyFillsCapacity() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(13L);
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.book(1L, 7L, 2, List.of());

        assertEquals(2, result.getNumberOfPeople());
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void book_ShouldCalculateTotalPrice_IncludingAddonsTimesPeople() {
        TripSession session = bookableSession(15, "20.00");
        AdditionalService transport = service(5L, "5.00");
        AdditionalService lunch = service(6L, "7.50");
        session.getTrip().getAdditionalServices().add(transport);
        session.getTrip().getAdditionalServices().add(lunch);

        stubCustomerAndSession(session);
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);
        when(additionalServices.findAllById(List.of(5L, 6L)))
                .thenReturn(List.of(transport, lunch));
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.book(1L, 7L, 3, List.of(5L, 6L));

        // 3 * 20.00 + 3 * 5.00 + 3 * 7.50 = 97.50
        assertEquals(0, new BigDecimal("97.50").compareTo(result.getTotalPrice()));
        assertEquals(2, result.getReservationAdditionalServices().size());
        assertTrue(result.getReservationAdditionalServices().stream()
                .allMatch(item -> item.getQuantity() == 3));
    }

    @Test
    void book_ShouldTreatNullAddonIdsAsEmptyList() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);
        when(additionalServices.findAllById(List.of())).thenReturn(List.of());
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.book(1L, 7L, 2, null);

        assertTrue(result.getReservationAdditionalServices().isEmpty());
        assertEquals(0, new BigDecimal("40.00").compareTo(result.getTotalPrice()));
    }

    @Test
    void book_ShouldThrowIllegalArgument_WhenAddonIdIsNull() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () ->
                reservationService.book(1L, 7L, 2, Arrays.asList(5L, null)));

        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowIllegalArgument_WhenSameAddonSelectedTwice() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () ->
                reservationService.book(1L, 7L, 2, List.of(5L, 5L)));

        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowIllegalArgument_WhenAddonDoesNotExist() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);
        when(additionalServices.findAllById(List.of(99L))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                reservationService.book(1L, 7L, 2, List.of(99L)));

        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void book_ShouldThrowIllegalArgument_WhenAddonIsNotOfferedByTrip() {
        TripSession session = bookableSession(15, "20.00"); // izlet ne nudi nijednu uslugu
        AdditionalService foreign = service(5L, "5.00");

        stubCustomerAndSession(session);
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);
        when(additionalServices.findAllById(List.of(5L))).thenReturn(List.of(foreign));

        assertThrows(IllegalArgumentException.class, () ->
                reservationService.book(1L, 7L, 2, List.of(5L)));

        verify(reservations, never()).save(any(Reservation.class));
    }

    // ---------------------------------------------------------------------
    // otkazivanje i dohvat: kodovi grešaka
    // ---------------------------------------------------------------------

    @Test
    void cancelReservation_ShouldThrowNotFoundWithCode_WhenCustomerIsNotOwner() {
        AppUser other = new AppUser();
        other.setId(2L);

        Reservation reservation = new Reservation();
        reservation.setCustomer(other);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservation(21L, 1L));

        assertEquals("RESERVATION_NOT_FOUND", e.getCode());
    }

    @Test
    void cancelReservation_ShouldThrowNotCancellable_WhenReservationIsCompleted() {
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.COMPLETED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.cancelReservation(21L, 1L));

        assertEquals("RESERVATION_NOT_CANCELLABLE", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservation_ShouldThrowNotCancellable_WhenTripHasStarted_WithCode() {
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().minusMinutes(5));

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.cancelReservation(21L, 1L));

        assertEquals("RESERVATION_NOT_CANCELLABLE", e.getCode());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    void getMyReservations_ShouldThrowNotFoundWithCode_WhenCustomerDoesNotExist() {
        when(users.existsById(99L)).thenReturn(false);

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.getMyReservations(99L));

        assertEquals("USER_NOT_FOUND", e.getCode());
    }

    // ---------------------------------------------------------------------
    // administratorsko otkazivanje
    // ---------------------------------------------------------------------

    @Test
    void cancelReservationAsAdmin_ShouldCancel_WhenReservationIsConfirmed() {
        AppUser someoneElse = new AppUser();
        someoneElse.setId(42L);

        Reservation reservation = new Reservation();
        reservation.setCustomer(someoneElse);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));
        when(reservations.save(reservation)).thenReturn(reservation);

        Reservation result = reservationService.cancelReservationAsAdmin(21L);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(reservations).save(reservation);
    }

    @Test
    void cancelReservationAsAdmin_ShouldThrowNotFound_WhenReservationDoesNotExist() {
        when(reservations.findDetailsById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservationAsAdmin(999L));

        assertEquals("RESERVATION_NOT_FOUND", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservationAsAdmin_ShouldThrowNotCancellable_WhenStatusIsNotConfirmed() {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.COMPLETED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));

        BusinessConflictException e = assertThrows(BusinessConflictException.class, () ->
                reservationService.cancelReservationAsAdmin(21L));

        assertEquals("RESERVATION_NOT_CANCELLABLE", e.getCode());
        verify(reservations, never()).save(any(Reservation.class));
    }

    // ---------------------------------------------------------------------
    // događaji za e-mail (šalju se tek nakon commita, vidi ReservationMailListener)
    // ---------------------------------------------------------------------

    @Test
    void book_ShouldPublishConfirmedEvent_WithSavedReservationId() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(0L);
        when(reservations.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(55L);
            return saved;
        });

        reservationService.book(1L, 7L, 2, List.of());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(captor.capture());
        assertEquals(new ReservationConfirmedEvent(55L), captor.getValue());
    }

    @Test
    void book_ShouldNotPublishEvent_WhenBookingFails() {
        stubCustomerAndSession(bookableSession(15, "20.00"));
        when(reservations.sumBookedSeats(7L)).thenReturn(15L);

        assertThrows(BusinessConflictException.class, () ->
                reservationService.book(1L, 7L, 2, List.of()));

        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    void cancelReservation_ShouldPublishCancelledEvent() {
        AppUser customer = new AppUser();
        customer.setId(1L);

        TripSession session = new TripSession();
        session.setStartTime(OffsetDateTime.now().plusDays(2));

        Reservation reservation = new Reservation();
        reservation.setId(21L);
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));
        when(reservations.save(reservation)).thenReturn(reservation);

        reservationService.cancelReservation(21L, 1L);

        verify(events).publishEvent(new ReservationCancelledEvent(21L));
    }

    @Test
    void cancelReservation_ShouldNotPublishEvent_WhenCancellationIsRejected() {
        when(reservations.findDetailsById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservation(999L, 1L));

        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    void cancelReservationAsAdmin_ShouldPublishCancelledEvent() {
        Reservation reservation = new Reservation();
        reservation.setId(21L);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));
        when(reservations.save(reservation)).thenReturn(reservation);

        reservationService.cancelReservationAsAdmin(21L);

        verify(events).publishEvent(new ReservationCancelledEvent(21L));
    }
}
