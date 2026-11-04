package touristAgency.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import touristAgency.events.ReservationCancelledEvent;
import touristAgency.events.ReservationConfirmedEvent;
import touristAgency.repositories.ReservationRepository;

@Slf4j
@Component
public class ReservationMailListener {

    private final ReservationRepository reservations;
    private final MailService mailService;

    public ReservationMailListener(ReservationRepository reservations, MailService mailService) {
        this.reservations = reservations;
        this.mailService = mailService;
    }

    @Async
    @TransactionalEventListener
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        try {
            reservations.findDetailsById(event.reservationId()).ifPresentOrElse(
                            mailService::sendReservationConfirmation,
                            () -> log.warn("Rezervacija {} nije pronađena, potvrda nije poslana.",event.reservationId()));
        } catch (RuntimeException e) {
            log.error("Slanje potvrde za rezervaciju {} nije uspjelo: {}",event.reservationId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener
    public void onReservationCancelled(ReservationCancelledEvent event) {
        try {
            reservations.findDetailsById(event.reservationId()).ifPresentOrElse(
                            mailService::sendReservationCancellation,
                            () -> log.warn("Rezervacija {} nije pronađena, obavijest o otkazivanju nije poslana.",
                                    event.reservationId()));
        } catch (RuntimeException e) {
            log.error("Slanje obavijesti o otkazivanju za rezervaciju {} nije uspjelo: {}",event.reservationId(), e.getMessage());
        }
    }
}