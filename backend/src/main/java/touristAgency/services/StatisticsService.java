package touristAgency.services;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import touristAgency.dto.StatisticsResponse;
import touristAgency.dto.TopItemResponse;
import touristAgency.dto.TripOccupancyResponse;
import touristAgency.exceptions.BadRequestException;
import touristAgency.repositories.ReservationAdditionalServiceRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.projection.StatisticsSummary;
import touristAgency.repositories.projection.TopItem;
import touristAgency.repositories.projection.TripOccupancy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ReservationRepository reservationRepository;
    private final ReservationAdditionalServiceRepository additionalServiceRepository;

    private static final ZoneId ZONE = ZoneId.of("Europe/Zagreb");

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Potrebno je zadati početni i završni datum.");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("Završni datum ne smije biti prije početnog.");
        }

        OffsetDateTime start = from.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime end = to.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();

        StatisticsSummary summary = reservationRepository.getStatisticsSummary(start, end);
        List<TripOccupancy> occupancy = reservationRepository.getTripOccupancy(start, end);
        List<TopItem> topCategories = reservationRepository.findTop5Categories(start, end);
        List<TopItem> topServices = additionalServiceRepository.findTop5AdditionalServices(start, end);

        return new StatisticsResponse(
                from,
                to,
                summary.getReservationCount(),
                summary.getCancelledCount(),
                summary.getRevenue(),
                occupancy.stream().map(TripOccupancyResponse::from).toList(),
                topCategories.stream().map(TopItemResponse::from).toList(),
                topServices.stream().map(TopItemResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public String exportCsv(LocalDate from, LocalDate to) {
        StatisticsResponse s = getStatistics(from, to);
        StringBuilder csv = new StringBuilder();

        csv.append(row("Razdoblje od", "Razdoblje do"));
        csv.append(row(s.from(), s.to()));
        csv.append("\n");

        csv.append(row("Broj rezervacija", "Otkazane rezervacije", "Prihod (EUR)"));
        csv.append(row(s.reservationCount(), s.cancelledCount(), s.revenue()));
        csv.append("\n");

        csv.append(row("Popunjenost izleta"));
        csv.append(row("Izlet", "Broj termina", "Kapacitet", "Rezervirano osoba", "Popunjenost (%)"));
        for (TripOccupancyResponse o : s.tripOccupancy()) {
            csv.append(row(o.name(), o.sessionCount(), o.capacity(), o.bookedPeople(), o.occupancyPercent()));
        }
        csv.append("\n");

        csv.append(row("Top 5 kategorija"));
        csv.append(row("Kategorija", "Broj rezervacija"));
        for (TopItemResponse c : s.topCategories()) {
            csv.append(row(c.name(), c.total()));
        }
        csv.append("\n");

        csv.append(row("Top 5 dodatnih usluga"));
        csv.append(row("Usluga", "Količina"));
        for (TopItemResponse a : s.topAdditionalServices()) {
            csv.append(row(a.name(), a.total()));
        }

        return csv.toString();
    }

    private static String row(Object... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(";");
            }
            line.append(cell(values[i]));
        }
        return line.append("\n").toString();
    }

    private static String cell(Object value) {
        String text = value == null ? "" : value.toString();
        if (text.contains(";") || text.contains("\"") || text.contains("\n")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}