package touristAgency.events;

public record ReservationCancelledEvent(Long reservationId, CancellationReason reason) {

    public ReservationCancelledEvent(Long reservationId) {
        this(reservationId, CancellationReason.REQUESTED);
    }
}
