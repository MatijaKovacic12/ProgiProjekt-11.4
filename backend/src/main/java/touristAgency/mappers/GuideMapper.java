package touristAgency.mappers;

import org.springframework.stereotype.Component;
import touristAgency.dto.GuideResponse;
import touristAgency.dto.GuideScheduleResponse;
import touristAgency.dto.GuideSessionResponse;
import touristAgency.dto.SessionGuidesResponse;
import touristAgency.models.AppUser;
import touristAgency.models.TripSession;

import java.util.List;

@Component
public class GuideMapper {

    public GuideResponse toGuideResponse(AppUser guide) {
        if (guide == null) return null;
        return GuideResponse.builder()
                .id(guide.getId())
                .firstName(guide.getFirstName())
                .lastName(guide.getLastName())
                .email(guide.getEmail())
                .build();
    }

    public GuideSessionResponse toGuideSessionResponse(TripSession session, String departurePoint, long bookedPeople) {
        if (session == null) return null;
        return GuideSessionResponse.builder()
                .sessionId(session.getId())
                .tripId(session.getTrip().getId())
                .tripName(session.getTrip().getName())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .departurePoint(departurePoint)
                .bookedPeople(bookedPeople)
                .build();
    }

    public GuideScheduleResponse toGuideScheduleResponse(AppUser guide, List<GuideSessionResponse> upcomingSessions) {
        return GuideScheduleResponse.builder()
                .id(guide.getId())
                .firstName(guide.getFirstName())
                .lastName(guide.getLastName())
                .email(guide.getEmail())
                .upcomingSessions(upcomingSessions)
                .build();
    }

    public SessionGuidesResponse toSessionGuidesResponse(TripSession session, List<AppUser> assignedGuides) {
        int required = session.getTrip().getRequiredGuides();
        return SessionGuidesResponse.builder()
                .sessionId(session.getId())
                .tripId(session.getTrip().getId())
                .tripName(session.getTrip().getName())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .requiredGuides(required)
                .assignedGuides(assignedGuides.stream().map(this::toGuideResponse).toList())
                .fullyStaffed(assignedGuides.size() >= required)
                .build();
    }
}
