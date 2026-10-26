package touristAgency.repositories.projection;

public interface TripOccupancy {
    Long getTripId();
    String getName();
    Long getSessionCount();
    Long getCapacity();
    Long getBookedPeople();
    Double getOccupancyPercent();
}
