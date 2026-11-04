package touristAgency.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import touristAgency.dto.AdminReservationResponse;
import touristAgency.mappers.ReservationMapper;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;
import touristAgency.services.ReservationService;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminReservationResponse>> searchReservations(
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false, name = "q") String term,
            @RequestParam(required = false) LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Page must be greater than or equal to 0");
        if (size <= 0 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Size must be between 1 and 100");


        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "createdAt"));

        String searchTerm = term == null ? "" : term.trim();

        Page<Reservation> reservationsPage = reservationService.searchReservations(tripId, sessionId, status, searchTerm,
                createdDate, pageable);

        return ResponseEntity.ok(reservationsPage.map(ReservationMapper::toAdminResponse));
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<AdminReservationResponse> cancelReservation(
            @PathVariable Long reservationId) {

        Reservation reservation = reservationService.cancelReservationAsAdmin(reservationId);

        return ResponseEntity.ok(ReservationMapper.toAdminResponse(reservation));
    }
}