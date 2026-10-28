package touristAgency.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import touristAgency.models.*;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    public boolean sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("turizam@test.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            log.error("Slanje maila na {} nije uspjelo: {}", to, e.getMessage());
            return false;
        }
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy. 'u' HH:mm");

    public boolean sendReservationConfirmation(Reservation r) {
        try {
            Trip trip = r.getTripSession().getTrip();

            String to = r.getCustomer().getEmail();
            String subject = "Potvrda rezervacije: " + trip.getName();

            String text = "Poštovani/a " + r.getCustomer().getFirstName() + ",\n\n"
                    + "vaša rezervacija je potvrđena.\n\n"
                    + "Izlet: " + trip.getName() + "\n"
                    + "Termin: " + r.getTripSession().getStartTime().format(DATE_FORMAT) + "\n"
                    + "Mjesto polaska: " + findDeparture(trip) + "\n"
                    + "Broj osoba: " + r.getNumberOfPeople() + "\n"
                    + "Dodatne usluge:\n" + listServices(r)
                    + "Ukupna cijena: " + r.getTotalPrice() + " EUR ";

            return sendMail(to, subject, text);
        } catch (RuntimeException e) {
            log.error("Ne mogu složiti potvrdu za rezervaciju {}: {}", r.getId(), e.getMessage());
            return false;
        }
    }

    private String findDeparture(Trip trip) {
        for (Location l : trip.getLocations()) {
            if (l.getType() == LocationType.DEPARTURE) {
                return l.getName();
            }
        }
        return "nije navedeno";
    }

    private String listServices(Reservation r) {
        if (r.getReservationAdditionalServices().isEmpty()) {
            return "- nema\n";
        }
        String services = "";
        for (ReservationAdditionalService s : r.getReservationAdditionalServices()) {
            services += "- " + s.getAdditionalService().getName() + " x" + s.getQuantity() + "\n";
        }
        return services;
    }
}