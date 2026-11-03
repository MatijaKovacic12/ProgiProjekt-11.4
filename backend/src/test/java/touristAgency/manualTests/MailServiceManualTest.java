package touristAgency.manualTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import touristAgency.models.*;
import touristAgency.services.MailService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MailServiceManualTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "MAIL_USERNAME", matches = ".+")
    void sendsTestMail() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(System.getenv("MAIL_USERNAME"));
        sender.setPassword(System.getenv("MAIL_PASSWORD"));
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");

        MailService mailService = new MailService(sender);

        assertTrue(mailService.sendMail("izleti.g114@gmail.com", "Test", "Radi napokon!"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "MAIL_USERNAME", matches = ".+")
    void sendsReservationConfirmation() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(System.getenv("MAIL_USERNAME"));
        sender.setPassword(System.getenv("MAIL_PASSWORD"));
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");
        MailService mailService = new MailService(sender);

        AppUser ana = new AppUser();
        ana.setFirstName("Ana");
        ana.setEmail("izleti.g114@gmail.com");

        Trip trip = new Trip();
        trip.setName("Rafting na Cetini");
        Location departure = new Location();
        departure.setName("Omiš, autobusni kolodvor");
        departure.setType(LocationType.DEPARTURE);
        trip.addLocation(departure);

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setStartTime(OffsetDateTime.of(2026, 11, 15, 9, 0, 0, 0, ZoneOffset.ofHours(1)));

        AdditionalService transport = new AdditionalService();
        transport.setName("Prijevoz");
        transport.setPrice(new BigDecimal("10.00"));

        Reservation r = new Reservation();
        r.setCustomer(ana);
        r.setTripSession(session);
        r.setNumberOfPeople(3);
        r.setTotalPrice(new BigDecimal("165.00"));
        r.addAdditionalService(transport, 3);

        assertTrue(mailService.sendReservationConfirmation(r));
    }
}