package touristAgency.services;

import touristAgency.repositories.ReservationAdditionalServiceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.events.ReservationCancelledEvent;
import touristAgency.events.ReservationConfirmedEvent;
import touristAgency.exceptions.BusinessConflictException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.models.AdditionalService;
import touristAgency.models.AppUser;
import touristAgency.models.Reservation;
import touristAgency.models.ReservationStatus;
import touristAgency.models.TripSession;
import touristAgency.repositories.AdditionalServiceRepository;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.TripSessionRepository;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservations;
    private final TripSessionRepository tripSessions;
    private final AdditionalServiceRepository additionalServices;
    private final AppUserRepository users;
    private final ReservationAdditionalServiceRepository reservationAdditionalServices;
    private final ApplicationEventPublisher events;

    public ReservationService(ReservationRepository reservations,TripSessionRepository tripSessions,
            AdditionalServiceRepository additionalServices,AppUserRepository users,
            ReservationAdditionalServiceRepository reservationAdditionalServices,ApplicationEventPublisher events) {

        this.reservations = reservations;
        this.tripSessions = tripSessions;
        this.additionalServices = additionalServices;
        this.users = users;
        this.reservationAdditionalServices = reservationAdditionalServices;
        this.events = events;
    }

    @Transactional
    public Reservation book(Long customerId,Long sessionId,int people,List<Long> addonIds) {

        if (customerId == null) throw new IllegalArgumentException("Korisnik nije naveden.");
        if (people <= 0) throw new IllegalArgumentException("Broj osoba mora biti veći od nule.");

        AppUser customer = users.findById(customerId).orElseThrow(() ->
                        new ResourceNotFoundException("USER_NOT_FOUND", "Korisnik ne postoji."));

        TripSession session = tripSessions.findByIdForUpdate(sessionId).orElseThrow(() ->
                        new ResourceNotFoundException("SESSION_NOT_FOUND", "Termin ne postoji."));

        OffsetDateTime now = OffsetDateTime.now();

        if (!session.isActive() || !session.getTrip().isActive() || !session.getStartTime().isAfter(now)) {
            throw new BusinessConflictException("SESSION_NOT_BOOKABLE","Termin više nije dostupan za rezervaciju.");
        }

        long bookedSeats = reservations.sumBookedSeats(sessionId);

        if (bookedSeats + people > session.getTrip().getMaxParticipants()) {
            throw new BusinessConflictException("NOT_ENOUGH_SEATS","Nema dovoljno slobodnih mjesta.");
        }

        BigDecimal totalPrice = session.getTrip().getPrice().multiply(BigDecimal.valueOf(people));

        if (addonIds == null) addonIds = List.of();
        if (addonIds.stream().anyMatch(id -> id == null)) {
            throw new IllegalArgumentException("ID dodatne usluge ne smije biti null.");
        }

        if (addonIds.size() != addonIds.stream().distinct().count()) {
            throw new IllegalArgumentException("Ista dodatna usluga ne smije biti odabrana više puta.");
        }

        List<AdditionalService> selectedServices =additionalServices.findAllById(addonIds);

        if (selectedServices.size() != addonIds.size()) {
            throw new IllegalArgumentException("Jedna ili više dodatnih usluga ne postoje.");
        }

        Set<Long> tripServiceIds = session.getTrip().getAdditionalServices().stream().map(AdditionalService::getId)
                .collect(Collectors.toSet());

        for (AdditionalService service : selectedServices) {
            if (!tripServiceIds.contains(service.getId())) {
                throw new IllegalArgumentException("Odabrana usluga nije dostupna za ovaj izlet.");
            }

            totalPrice = totalPrice.add(service.getPrice().multiply(BigDecimal.valueOf(people)));
        }

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setTripSession(session);
        reservation.setNumberOfPeople(people);
        reservation.setTotalPrice(totalPrice);

        for (AdditionalService service : selectedServices) {
            reservation.addAdditionalService(service, people);
        }

        Reservation saved = reservations.save(reservation);

        events.publishEvent(new ReservationConfirmedEvent(saved.getId()));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getMyReservations(Long customerId) {

        if (!users.existsById(customerId)) throw new ResourceNotFoundException("USER_NOT_FOUND","Korisnik ne postoji.");

        return reservations.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationDetails(Long reservationId, Long customerId) {

        Reservation reservation =reservations.findDetailsById(reservationId).orElseThrow(() ->
                                new ResourceNotFoundException("RESERVATION_NOT_FOUND","Rezervacija ne postoji."));

        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("RESERVATION_NOT_FOUND","Rezervacija ne postoji.");
        }

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId,Long customerId) {

        Reservation reservation =reservations.findDetailsById(reservationId).orElseThrow(() ->
                                new ResourceNotFoundException("RESERVATION_NOT_FOUND","Rezervacija ne postoji."));

        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException("RESERVATION_NOT_FOUND","Rezervacija ne postoji.");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessConflictException("RESERVATION_NOT_CANCELLABLE","Može se otkazati samo potvrđena rezervacija.");
        }

        if (!reservation.getTripSession().getStartTime().isAfter(OffsetDateTime.now())) {

            throw new BusinessConflictException("RESERVATION_NOT_CANCELLABLE",
                    "Rezervacija za termin koji je započeo ne može se otkazati.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation saved = reservations.save(reservation);
        events.publishEvent(new ReservationCancelledEvent(saved.getId()));

        return saved;
    }

    @Transactional
    public Reservation cancelReservationAsAdmin(Long reservationId) {

        Reservation reservation =reservations.findDetailsById(reservationId).orElseThrow(() ->
                                new ResourceNotFoundException("RESERVATION_NOT_FOUND","Rezervacija ne postoji."));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessConflictException("RESERVATION_NOT_CANCELLABLE","Može se otkazati samo potvrđena rezervacija.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation saved = reservations.save(reservation);
        events.publishEvent(new ReservationCancelledEvent(saved.getId()));

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Reservation> searchReservations(Long tripId,Long sessionId,ReservationStatus status,String term,
            LocalDate createdDate,Pageable pageable) {

        String searchTerm = term == null ? "" : term.trim();
        if (createdDate == null) return reservations.searchAdminWithoutDate(tripId,sessionId,status,searchTerm,pageable);

        return reservations.searchAdmin(tripId,sessionId,status,searchTerm,createdDate,pageable);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getActiveInProgress(Long customerId) {

        if (!users.existsById(customerId)) throw new ResourceNotFoundException("USER_NOT_FOUND","Korisnik ne postoji.");

        return reservations.findActiveInProgress(customerId,OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public StatisticsSummary getStatisticsSummary(OffsetDateTime dateFrom,OffsetDateTime dateTo) {

        validatePeriod(dateFrom, dateTo);

        return reservations.getStatisticsSummary(dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<TripOccupancy> getTripOccupancy(OffsetDateTime dateFrom,OffsetDateTime dateTo) {

        validatePeriod(dateFrom, dateTo);

        return reservations.getTripOccupancy(dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<TopItem> getTop5Categories(OffsetDateTime dateFrom,OffsetDateTime dateTo) {

        validatePeriod(dateFrom, dateTo);

        return reservations.findTop5Categories(dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<TopItem> getTop5AdditionalServices(OffsetDateTime dateFrom,OffsetDateTime dateTo) {

        validatePeriod(dateFrom, dateTo);

        return reservationAdditionalServices.findTop5AdditionalServices(dateFrom, dateTo);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void completeFinishedReservations() {
        reservations.markFinished(OffsetDateTime.now());
    }

    private void validatePeriod(OffsetDateTime dateFrom,OffsetDateTime dateTo) {

        if (dateFrom == null || dateTo == null || !dateFrom.isBefore(dateTo)) {
            throw new IllegalArgumentException("Neispravno razdoblje za statistiku.");
        }
    }
}