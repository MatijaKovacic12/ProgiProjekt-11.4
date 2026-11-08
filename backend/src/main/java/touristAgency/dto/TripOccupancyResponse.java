package touristAgency.dto;

import touristAgency.repositories.projection.TripOccupancy;

public record TripOccupancyResponse (
        Long tripId,
        String name,
        Long sessionCount,
        Long capacity,
        Long bookedPeople,
        Double occupancyPercent) {

    public static TripOccupancyResponse from(TripOccupancy o) {
        return new TripOccupancyResponse(
                o.getTripId(),
                o.getName(),
                o.getSessionCount(),
                o.getCapacity(),
                o.getBookedPeople(),
                o.getOccupancyPercent());
    }
}

