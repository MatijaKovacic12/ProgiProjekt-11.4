package touristAgency.unitTests.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import touristAgency.models.*;
import touristAgency.services.MailService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private JavaMailSender mailSender;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mailService = new MailService(mailSender);
    }

    private SimpleMailMessage sentMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void sendMail_sendsMessageWithGivenData() {
        boolean sent = mailService.sendMail("ana@test.com", "Naslov", "Tekst");

        SimpleMailMessage msg = sentMessage();
        assertTrue(sent);
        assertArrayEquals(new String[]{"ana@test.com"}, msg.getTo());
        assertEquals("Naslov", msg.getSubject());
        assertEquals("Tekst", msg.getText());
    }

    @Test
    void sendMail_returnsFalseWhenServerFails() {
        doThrow(new MailSendException("Server ne radi"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        boolean sent = mailService.sendMail("ana@test.com", "Naslov", "Tekst");

        assertFalse(sent);
    }

    @Test
    void reservationConfirmation_containsAllDetails() {
        Reservation r = sampleReservation(true);

        boolean sent = mailService.sendReservationConfirmation(r);

        SimpleMailMessage msg = sentMessage();
        String text = msg.getText();
        assertTrue(sent);
        assertArrayEquals(new String[]{"ana@test.com"}, msg.getTo());
        assertEquals("Potvrda rezervacije: Rafting na Cetini", msg.getSubject());
        assertTrue(text.contains("Izlet: Rafting na Cetini"));
        assertTrue(text.contains("Termin: 15.11.2026. u 09:00"));
        assertTrue(text.contains("Mjesto polaska: Omiš, autobusni kolodvor"));
        assertTrue(text.contains("Broj osoba: 3"));
        assertTrue(text.contains("- Prijevoz x3"));
        assertTrue(text.contains("Ukupna cijena: 165.00 EUR"));
    }

    @Test
    void reservationConfirmation_handlesMissingDepartureAndServices() {
        Reservation r = sampleReservation(false);

        mailService.sendReservationConfirmation(r);

        String text = sentMessage().getText();
        assertTrue(text.contains("Mjesto polaska: nije navedeno"));
        assertTrue(text.contains("- nema"));
    }

    @Test
    void reservationConfirmation_returnsFalseWhenServerFails() {
        doThrow(new MailSendException("Server ne radi"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        boolean sent = mailService.sendReservationConfirmation(sampleReservation(true));

        assertFalse(sent);
    }

    @Test
    void reservationConfirmation_returnsFalseForIncompleteReservation() {
        Reservation r = new Reservation();

        boolean sent = mailService.sendReservationConfirmation(r);

        assertFalse(sent);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    private Reservation sampleReservation(boolean full) {
        AppUser ana = new AppUser();
        ana.setFirstName("Ana");
        ana.setEmail("ana@test.com");

        Trip trip = new Trip();
        trip.setName("Rafting na Cetini");
        if (full) {
            Location departure = new Location();
            departure.setName("Omiš, autobusni kolodvor");
            departure.setType(LocationType.DEPARTURE);
            trip.addLocation(departure);
        }

        TripSession session = new TripSession();
        session.setTrip(trip);
        session.setStartTime(OffsetDateTime.of(2026, 11, 15, 9, 0, 0, 0, ZoneOffset.ofHours(1)));

        Reservation r = new Reservation();
        r.setCustomer(ana);
        r.setTripSession(session);
        r.setNumberOfPeople(3);
        r.setTotalPrice(new BigDecimal("165.00"));

        if (full) {
            AdditionalService transport = new AdditionalService();
            transport.setName("Prijevoz");
            transport.setPrice(new BigDecimal("10.00"));
            r.addAdditionalService(transport, 3);
        }
        return r;
    }
}