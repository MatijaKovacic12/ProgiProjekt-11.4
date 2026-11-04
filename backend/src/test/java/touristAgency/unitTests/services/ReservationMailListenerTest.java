package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import touristAgency.events.ReservationCancelledEvent;
import touristAgency.events.ReservationConfirmedEvent;
import touristAgency.models.Reservation;
import touristAgency.repositories.ReservationRepository;
import touristAgency.services.MailService;
import touristAgency.services.ReservationMailListener;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationMailListenerTest {

    @Mock
    private ReservationRepository reservations;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ReservationMailListener listener;

    @Test
    void onReservationConfirmed_ShouldSendConfirmation_WhenReservationExists() {
        Reservation reservation = new Reservation();
        when(reservations.findDetailsById(55L)).thenReturn(Optional.of(reservation));

        listener.onReservationConfirmed(new ReservationConfirmedEvent(55L));

        verify(mailService).sendReservationConfirmation(reservation);
        verify(mailService, never()).sendReservationCancellation(any());
    }

    @Test
    void onReservationConfirmed_ShouldNotSend_WhenReservationIsMissing() {
        when(reservations.findDetailsById(55L)).thenReturn(Optional.empty());

        listener.onReservationConfirmed(new ReservationConfirmedEvent(55L));

        verifyNoInteractions(mailService);
    }

    @Test
    void onReservationConfirmed_ShouldNotThrow_WhenLoadingFails() {
        when(reservations.findDetailsById(55L)).thenThrow(new RuntimeException("baza ne radi"));

        assertDoesNotThrow(() -> listener.onReservationConfirmed(new ReservationConfirmedEvent(55L)));

        verifyNoInteractions(mailService);
    }

    @Test
    void onReservationCancelled_ShouldSendCancellation_WhenReservationExists() {
        Reservation reservation = new Reservation();
        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));

        listener.onReservationCancelled(new ReservationCancelledEvent(21L));

        verify(mailService).sendReservationCancellation(reservation);
        verify(mailService, never()).sendReservationConfirmation(any());
    }

    @Test
    void onReservationCancelled_ShouldNotThrow_WhenMailFails() {
        Reservation reservation = new Reservation();
        when(reservations.findDetailsById(21L)).thenReturn(Optional.of(reservation));
        when(mailService.sendReservationCancellation(reservation))
                .thenThrow(new RuntimeException("mail server ne radi"));

        assertDoesNotThrow(() -> listener.onReservationCancelled(new ReservationCancelledEvent(21L)));
    }
}
