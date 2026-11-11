package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/** Termin s dodijeljenim i potrebnim brojem vodiča (FR-14, UC-14). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionGuidesResponse {
    private Long sessionId;
    private Long tripId;
    private String tripName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer requiredGuides;
    private List<GuideResponse> assignedGuides;
    private boolean fullyStaffed;
}
