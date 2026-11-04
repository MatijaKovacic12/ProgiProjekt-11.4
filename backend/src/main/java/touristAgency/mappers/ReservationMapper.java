package touristAgency.mappers;

import touristAgency.dto.AdminReservationResponse;
import touristAgency.dto.MyReservationResponse;
import touristAgency.dto.ReservationCreateResponse;
import touristAgency.dto.ReservationDetailsResponse;
import touristAgency.models.AppUser;
import touristAgency.models.LocationType;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationAdditionalService;
import touristAgency.models.Trip;
import touristAgency.models.TripSession;
import java.util.List;

public final class ReservationMapper {

    private ReservationMapper() {
    }

    public static ReservationCreateResponse toCreateResponse(Reservation reservation) {
        TripSession session = reservation.getTripSession();

        List<ReservationCreateResponse.AddonResponse> addons = reservation
                .getReservationAdditionalServices().stream()
                .map(ReservationAdditionalService::getAdditionalService)
                .map(service -> new ReservationCreateResponse.AddonResponse(
                        service.getId(), service.getName())).toList();

        return new ReservationCreateResponse(reservation.getId(),session.getId(),session.getTrip().getName(),
                session.getStartTime(),reservation.getNumberOfPeople(),addons,reservation.getTotalPrice(),
                reservation.getStatus());
    }

    public static MyReservationResponse toMyResponse(Reservation reservation) {
        TripSession session = reservation.getTripSession();
        Trip trip = session.getTrip();

        String departurePoint = trip.getLocations().stream().filter(location -> location.getType() == LocationType.DEPARTURE)
                .map(location -> location.getName()).findFirst().orElse(null);

        return new MyReservationResponse(reservation.getId(),trip.getId(),session.getId(),trip.getName(),
                session.getStartTime(),departurePoint,reservation.getNumberOfPeople(),addonNames(reservation),
                reservation.getTotalPrice(),reservation.getStatus());
    }

    public static ReservationDetailsResponse toDetailsResponse(Reservation reservation) {
        TripSession session = reservation.getTripSession();
        Trip trip = session.getTrip();

        List<Long> addonIds = reservation.getReservationAdditionalServices().stream()
                .map(item -> item.getAdditionalService().getId()).toList();

        return new ReservationDetailsResponse(reservation.getId(),trip.getId(),session.getId(),trip.getName(),
                session.getStartTime(),reservation.getNumberOfPeople(),addonIds,reservation.getTotalPrice(),
                reservation.getStatus());
    }

    public static AdminReservationResponse toAdminResponse(Reservation reservation) {
        TripSession session = reservation.getTripSession();
        Trip trip = session.getTrip();
        AppUser customer = reservation.getCustomer();

        return new AdminReservationResponse(reservation.getId(),trip.getId(),trip.getName(),session.getId(),
                session.getStartTime(),customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),reservation.getNumberOfPeople(),addonNames(reservation),
                reservation.getStatus(),reservation.getCreatedAt(),reservation.getTotalPrice());
    }

    private static List<String> addonNames(Reservation reservation) {
        return reservation.getReservationAdditionalServices().stream()
                .map(item -> item.getAdditionalService().getName()).toList();
    }
}