package touristAgency.mappers;

import org.springframework.stereotype.Component;
import touristAgency.dto.AdditionalServiceResponse;
import touristAgency.dto.LocationResponse;
import touristAgency.dto.TripDetailsResponse;
import touristAgency.dto.TripSessionResponse;
import touristAgency.dto.TripSummaryResponse;
import touristAgency.models.AdditionalService;
import touristAgency.models.Location;
import touristAgency.models.Trip;
import touristAgency.models.TripSession;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TripMapper {

    /**
     * Pretvara minute u format prilagođen korisniku (npr. 150 -> "2.5 hours", 60 -> "1 hour", 45 -> "45 mins").
     */
    public String formatDuration(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "N/A";
        }
        if (minutes < 60) {
            return minutes + " mins";
        }
        double hours = minutes / 60.0;
        if (minutes % 60 == 0) {
            long wholeHours = minutes / 60;
            return wholeHours + (wholeHours == 1 ? " hour" : " hours");
        }
        return String.format(java.util.Locale.US, "%.1f hours", hours);
    }

    /**
     * Pomoćna metoda za pretvaranje korisničkog unosa (npr. "3 hours") natrag u minute (180).
     */
    public Integer parseDurationToMinutes(String duration) {
        if (duration == null || duration.isBlank()) {
            return null;
        }
        try {
            String lower = duration.toLowerCase().trim();
            if (lower.contains("hour")) {
                String numStr = lower.replaceAll("[^0-9.]", "");
                double hours = Double.parseDouble(numStr);
                return (int) Math.round(hours * 60);
            }
            if (lower.contains("min")) {
                String numStr = lower.replaceAll("[^0-9]", "");
                return Integer.parseInt(numStr);
            }
            return Integer.parseInt(lower.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Preslikava bazu Trip u sažeti DTO za kartice na pretrazi (GET /api/trips).
     */
    public TripSummaryResponse toSummaryResponse(Trip trip, String mainLocation, String departurePoint, List<String> timeSlots) {
        String categoryName = trip.getCategory() != null ? trip.getCategory().getName() : null;
        List<String> services = trip.getAdditionalServices() != null
                ? trip.getAdditionalServices().stream().map(AdditionalService::getName).sorted().collect(Collectors.toList())
                : Collections.emptyList();

        return TripSummaryResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .title(trip.getName())
                .description(trip.getDescription())
                .categoryId(trip.getCategory() != null ? trip.getCategory().getId() : null)
                .categoryName(categoryName)
                .activityCategory(categoryName)
                .durationMinutes(trip.getDurationMinutes())
                .duration(formatDuration(trip.getDurationMinutes()))
                .price(trip.getPrice())
                .maxParticipants(trip.getMaxParticipants())
                .requiredGuides(trip.getRequiredGuides())
                .requiredGuideCount(trip.getRequiredGuides())
                .location(mainLocation)
                .departurePoint(departurePoint)
                .active(trip.isActive())
                .additionalServices(services)
                .scheduledTimeSlots(timeSlots != null ? timeSlots : Collections.emptyList())
                .build();
    }

    /**
     * Preslikava Trip u detaljni DTO za stranicu pojedinog izleta (GET /api/trips/{id}).
     */
    public TripDetailsResponse toDetailsResponse(Trip trip,
                                                 String mainLocation,
                                                 String departurePoint,
                                                 List<AdditionalServiceResponse> services,
                                                 List<TripSessionResponse> sessions,
                                                 List<LocationResponse> landmarks) {
        String categoryName = trip.getCategory() != null ? trip.getCategory().getName() : null;
        List<String> slotStrings = sessions != null
                ? sessions.stream()
                .map(s -> s.getStartTime() != null ? s.getStartTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : "")
                .collect(Collectors.toList())
                : Collections.emptyList();

        return TripDetailsResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .title(trip.getName())
                .description(trip.getDescription())
                .categoryId(trip.getCategory() != null ? trip.getCategory().getId() : null)
                .categoryName(categoryName)
                .activityCategory(categoryName)
                .durationMinutes(trip.getDurationMinutes())
                .duration(formatDuration(trip.getDurationMinutes()))
                .price(trip.getPrice())
                .maxParticipants(trip.getMaxParticipants())
                .requiredGuides(trip.getRequiredGuides())
                .requiredGuideCount(trip.getRequiredGuides())
                .location(mainLocation)
                .departurePoint(departurePoint)
                .active(trip.isActive())
                .additionalServices(services != null ? services : Collections.emptyList())
                .scheduledSessions(sessions != null ? sessions : Collections.emptyList())
                .scheduledTimeSlots(slotStrings)
                .landmarks(landmarks != null ? landmarks : Collections.emptyList())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    /**
     * Preslikava entitet Location u LocationResponse.
     */
    public LocationResponse toLocationResponse(Location loc) {
        if (loc == null) return null;
        return LocationResponse.builder()
                .id(loc.getId())
                .name(loc.getName())
                .description(loc.getDescription())
                .significance(loc.getSignificance())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .type(loc.getType())
                .build();
    }

    /**
     * Preslikava entitet AdditionalService u AdditionalServiceResponse.
     */
    public AdditionalServiceResponse toAdditionalServiceResponse(AdditionalService service) {
        if (service == null) return null;
        return AdditionalServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .build();
    }

    /**
     * Preslikava termin TripSession u TripSessionResponse s izračunatim slobodnim mjestima.
     */
    public TripSessionResponse toTripSessionResponse(TripSession session, Long availableSeats) {
        if (session == null) return null;
        return TripSessionResponse.builder()
                .id(session.getId())
                .tripId(session.getTrip() != null ? session.getTrip().getId() : null)
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .availableSeats(availableSeats)
                .active(session.isActive())
                .build();
    }
}