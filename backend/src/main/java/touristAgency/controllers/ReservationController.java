package touristAgency.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import touristAgency.dto.MyReservationResponse;
import touristAgency.dto.ReservationCreateRequest;
import touristAgency.dto.ReservationCreateResponse;
import touristAgency.dto.ReservationDetailsResponse;
import touristAgency.mappers.ReservationMapper;
import touristAgency.models.Reservation;
import touristAgency.services.CurrentUserService;
import touristAgency.services.ReservationService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    public ReservationController(ReservationService reservationService,CurrentUserService currentUserService) {
        this.reservationService = reservationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request) {

        Reservation reservation = reservationService.book(currentUserService.requireUserId(),request.sessionId(),
                request.participants(),request.addonIds());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(reservation.getId()).toUri();

        return ResponseEntity.created(location).body(ReservationMapper.toCreateResponse(reservation));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations() {
        List<MyReservationResponse> response = reservationService
                .getMyReservations(currentUserService.requireUserId()).stream()
                .map(ReservationMapper::toMyResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailsResponse> getReservationDetails(
            @PathVariable Long reservationId) {

        Reservation reservation = reservationService.getReservationDetails(reservationId, currentUserService.requireUserId());

        return ResponseEntity.ok(ReservationMapper.toDetailsResponse(reservation));
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationDetailsResponse> cancelReservation(
            @PathVariable Long reservationId) {

        Reservation reservation = reservationService.cancelReservation(reservationId, currentUserService.requireUserId());

        return ResponseEntity.ok(ReservationMapper.toDetailsResponse(reservation));
    }
}