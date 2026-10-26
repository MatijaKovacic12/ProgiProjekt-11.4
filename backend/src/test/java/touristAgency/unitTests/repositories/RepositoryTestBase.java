package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.models.*;
import touristAgency.repositories.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class RepositoryTestBase {
    @Autowired
    protected AppUserRepository users;
    @Autowired protected ExternalIdentityRepository externalIdentities;
    @Autowired protected CategoryRepository categories;
    @Autowired protected AdditionalServiceRepository additionalServices;
    @Autowired protected ImageRepository images;
    @Autowired protected TripRepository trips;
    @Autowired protected LocationRepository locations;
    @Autowired protected TripSessionRepository tripSessions;
    @Autowired protected TripSessionGuideRepository tripSessionGuides;
    @Autowired protected ReservationRepository reservations;
    @Autowired protected ReservationAdditionalServiceRepository reservationAdditionalServices;

    protected OffsetDateTime now;

    protected AppUser customer;
    protected AppUser guide1;
    protected AppUser guide2;
    protected AppUser admin;

    protected Category hiking;
    protected AdditionalService transport;
    protected AdditionalService gear;

    protected Trip trip;

    protected TripSession upcoming;
    protected TripSession inProgress;
    protected TripSession past;

    protected Reservation upcomingRes;
    protected Reservation cancelledRes;
    protected Reservation inProgressRes;
    protected Reservation pastRes;

    @BeforeEach
    void seed() {
        now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        customer = user("Ana", "Horvat", "Ana@Test.hr", Role.USER);
        guide1 = user("Ivo", "Babić", "ivo@test.hr", Role.GUIDE);
        guide2 = user("Marko", "Zorić", "marko@test.hr", Role.GUIDE);
        admin = user("Iva", "Admin", "admin@test.hr", Role.ADMIN);

        ExternalIdentity identity = new ExternalIdentity();
        identity.setUser(customer);
        identity.setProvider("microsoft");
        identity.setProviderUserId("sub-123");
        externalIdentities.save(identity);

        hiking = new Category();
        hiking.setName("Planinarenje");
        categories.save(hiking);

        transport = additionalService("Prijevoz", "15.00");
        gear = additionalService("Najam opreme", "10.00");

        trip = new Trip();
        trip.setName("Plitvice");
        trip.setDescription("Jezera i slapovi");
        trip.setCategory(hiking);
        trip.setDurationMinutes(240);
        trip.setPrice(new BigDecimal("50.00"));
        trip.setMaxParticipants(10);
        trip.setRequiredGuides(1);
        trip.getAdditionalServices().add(transport);
        trip.addLocation(location("NP Plitvička jezera", LocationType.MAIN, 44.8654, 15.5820));
        trip.addLocation(location("Kolodvor Zagreb", LocationType.DEPARTURE, 45.8045, 15.9930));
        trip.addLocation(location("Veliki slap", LocationType.LANDMARK, 44.9020, 15.6100));
        trip.addLocation(location("Kozjak", LocationType.LANDMARK, 44.8890, 15.6040));
        trips.save(trip);

        upcoming = session(trip, now.plusDays(3));
        inProgress = session(trip, now.minusHours(1));
        past = session(trip, now.minusDays(2));

        upcomingRes = reservation(customer, upcoming, 7, ReservationStatus.CONFIRMED, "365.00");
        upcomingRes.addAdditionalService(transport, 7);
        reservations.save(upcomingRes);

        cancelledRes = reservation(customer, upcoming, 2, ReservationStatus.CANCELLED, "100.00");
        inProgressRes = reservation(customer, inProgress, 1, ReservationStatus.CONFIRMED, "50.00");
        pastRes = reservation(customer, past, 4, ReservationStatus.CONFIRMED, "200.00");

        tripSessionGuides.save(new TripSessionGuide(upcoming, guide1));

        reservations.flush();
    }


    protected AppUser user(String firstName, String lastName, String email, Role role) {
        AppUser u = new AppUser();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setRole(role);
        return users.save(u);
    }

    protected AdditionalService additionalService(String name, String price) {
        AdditionalService s = new AdditionalService();
        s.setName(name);
        s.setPrice(new BigDecimal(price));
        return additionalServices.save(s);
    }

    protected Location location(String name, LocationType type, double lat, double lon) {
        Location l = new Location();
        l.setName(name);
        l.setDescription("Opis: " + name);
        l.setLatitude(BigDecimal.valueOf(lat));
        l.setLongitude(BigDecimal.valueOf(lon));
        l.setType(type);
        return l;
    }

    protected TripSession session(Trip trip, OffsetDateTime start) {
        TripSession s = new TripSession();
        s.setTrip(trip);
        s.schedule(start);
        return tripSessions.save(s);
    }

    protected Reservation reservation(AppUser customer, TripSession session, int people,
                                      ReservationStatus status, String totalPrice) {
        Reservation r = new Reservation();
        r.setCustomer(customer);
        r.setTripSession(session);
        r.setNumberOfPeople(people);
        r.setStatus(status);
        r.setTotalPrice(new BigDecimal(totalPrice));
        return reservations.save(r);
    }
}
